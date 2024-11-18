# api 제어 및 스크립트 제어 중앙컨트롤기 = 넥서스
# made 경혁수
# author 이민호


import pandas as pd
import glob
import os
import re
import pickle
from datetime import datetime
from flask import Flask, request, jsonify
from statsmodels.tsa.arima.model import ARIMA
import db_query
from apscheduler.schedulers.background import BackgroundScheduler
import subprocess
import os
import datetime

app = Flask(__name__)

# 스크립트 실행 함수
def run_script(script_name):
    try:
        print(f"[{datetime.datetime.now()}] Running script: {script_name}")
        script_path = os.path.join("scripts", script_name)
        result = subprocess.run(["python", script_path], capture_output=True, text=True)
        print(f"[{datetime.datetime.now()}] Script output:\n{result.stdout}")
        if result.stderr:
            print(f"[{datetime.datetime.now()}] Script error:\n{result.stderr}")
    except Exception as e:
        print(f"Error while running script {script_name}: {e}")

# 스케줄러 초기화
scheduler = BackgroundScheduler()


# 개별 스케줄링
scheduler.add_job(func=lambda: run_script("Request_parking_filtered_1.py"),
                  trigger="cron", minute=4, second=50, id="collect_1")  # 매시간 4분 50초
scheduler.add_job(func=lambda: run_script("Request_parking_filtered_2.py"),
                  trigger="cron", minute=4, second=55, id="collect_2")  # 매시간 4분 55초
scheduler.add_job(func=lambda: run_script("Delete_parking_info.py"),
                  trigger="cron", minute=5, second=0, id="delete_parking_info")  # 매시간 5분
scheduler.add_job(func=lambda: run_script("all_filterd.py"),
                  trigger="cron", hour=0, id="all_filtered")  # 매ㅇㄹ 자정

#인덱스 페이지
@app.route('/', methods=['GET'])
def index():
    return "index page"

# Flask 엔드포인트: 즉시 작업 실행
@app.route('/run_scripts', methods=['POST'])
def run_scripts():
    scripts_to_run = [
        "Request_parking_filtered_1.py",
        "Request_parking_filtered_2.py",
        "Request_parking_filtered_3.py",
        "all_filterd.py",
        "Delete_parking_info.py"
    ]
    for script in scripts_to_run:
        run_script(script)
    return jsonify({"message": "Scripts executed manually"}), 200


# Flask 엔드포인트: 특정 스크립트 실행
@app.route('/run_script', methods=['POST'])
def run_script_endpoint():
    data = request.get_json()
    script_name = data.get('script_name')
    if not script_name:
        return jsonify({"error": "script_name is required"}), 400
    run_script(script_name)
    return jsonify({"message": f"Script {script_name} executed"}), 200


# 모델 학습 함수
def train_model(base_directory, model_directory="models"):
    # CSV 파일 전체 데이터를 준비
    csv_files = glob.glob(os.path.join(base_directory, "*.csv"))
    data_list = []

    for file in csv_files:
        df = pd.read_csv(file)
        df_selected = df[['PARKING_NAME', 'CUR_PARKING', 'CUR_PARKING_TIME']].copy()
        df_selected.loc[:, 'CUR_PARKING_TIME'] = pd.to_datetime(df_selected['CUR_PARKING_TIME'])
        data_list.append(df_selected)

    df_combined = pd.concat(data_list, ignore_index=True)
    df_combined = df_combined.sort_values('CUR_PARKING_TIME')
    df_combined.set_index('CUR_PARKING_TIME', inplace=True)

    # 모델 저장 디렉토리가 없다면 생성
    os.makedirs(model_directory, exist_ok=True)

    # 오늘 날짜로 모델 파일명 생성
    date_str = datetime.now().strftime('%Y%m%d')
    model_filename = f'arima_parking_model_{date_str}.pkl'
    model_filepath = os.path.join(model_directory, model_filename)

    # ARIMA 모델 학습 및 저장
    model = ARIMA(df_combined['CUR_PARKING'], order=(5, 1, 0))
    model_fit = model.fit()
    with open(model_filepath, 'wb') as file:
        pickle.dump(model_fit, file)

    return model_filepath  # 저장된 모델 파일 경로 반환

# 예측 함수
def predict_parking(model_fit, parking_name, df_combined):
    df_specific = df_combined[df_combined['PARKING_NAME'] == parking_name]
    if df_specific.empty:
        return {"error": f"No data found for parking name '{parking_name}'"}

    forecast_steps = 24
    forecast = model_fit.forecast(steps=forecast_steps)
    average_forecast = int(forecast.mean())
    result = {
        'parking_name': parking_name,
        'predicted_cur_parking': average_forecast
    }
    return result

# 모델 학습 엔드포인트
@app.route('/train_model', methods=['POST'])
def train_model_endpoint():
    base_directory = request.json.get('base_directory', "/Tmap_project_Data/combined/")
    model_filepath = train_model(base_directory)
    return jsonify({"message": "Model trained and saved successfully", "model_path": model_filepath}), 200

# 예측 엔드포인트
@app.route('/predict', methods=['POST'])
def predict_endpoint():
    data = request.get_json()
    date_str = data.get('date', datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    parking_name = data.get('parking_name')
    
    if not parking_name:
        return jsonify({"error": "parking_name parameter is required"}), 400

    model_directory = "models"
    latest_model_file = sorted(glob.glob(os.path.join(model_directory, 'arima_parking_model_*.pkl')))[-1]
    try:
        with open(latest_model_file, 'rb') as file:
            loaded_model = pickle.load(file)
    except FileNotFoundError:
        return jsonify({"error": "Model not found. Please train the model first."}), 500

    base_directory = "/Users/kyung/PycharmProjects/FlaskServer/수집데이터_1"
    df_combined = train_model(base_directory)  # 데이터를 다시 준비

    result = predict_parking(loaded_model, parking_name, df_combined)
    result['date'] = date_str
    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8080)
