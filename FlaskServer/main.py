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
from apscheduler.schedulers.background import BackgroundScheduler
import threading
import subprocess
import time
import logging

# 로그 설정
logging.basicConfig(level=logging.INFO)
logging.getLogger('apscheduler').setLevel(logging.DEBUG)

app = Flask(__name__)


# 스크립트 실행 함수
def run_script(script_name):
    try:
        script_path = os.path.abspath(os.path.join("scripts", script_name))
        if not os.path.isfile(script_path):
            raise FileNotFoundError(f"Script {script_name} not found at {script_path}")
        result = subprocess.run(["python", script_path], capture_output=True, text=True)
        logging.info(f"Running script: {script_name}")
        logging.info(f"Output: {result.stdout}")
        if result.stderr:
            logging.error(f"Error: {result.stderr}")
    except Exception as e:
        logging.error(f"Exception while running script {script_name}: {e}")

# 스케줄러 초기화
scheduler = BackgroundScheduler()

# Delete 실행: 매 5분마다 실행
scheduler.add_job(func=lambda: run_script("Delete_parking_info.py"),
                  trigger="cron", minute="*/5", id="delete_parking_info")

# Request 1 실행: Delete 실행 후 10초 대기 후 실행
scheduler.add_job(func=lambda: [
    time.sleep(10),
    run_script("Request_parking_filtered_1.py")
], trigger="cron", minute="*/5", id="request_parking_1")

# Request 2 실행: Request 1 실행 후 10초 대기 후 실행
scheduler.add_job(func=lambda: [
    time.sleep(20),
    run_script("Request_parking_filtered_2.py")
], trigger="cron", minute="*/5", id="request_parking_2")

# 자정에 all_filtered 실행
scheduler.add_job(func=lambda: run_script("all_filterd.py"),
                  trigger="cron", hour=0, minute=0, id="all_filtered")

# 자정에 모델 학습
# scheduler.add_job(func=lambda: run_train_model(),
#                   trigger="cron", hour=1, minute=10, id="train_model_scheduler")

# 스케줄러 스레드 시작
def start_scheduler():
    scheduler.start()

# Flask 엔드포인트
@app.route('/', methods=['GET'])
def index():
    return "Index Page - Parking Scheduler"

@app.route('/run_script', methods=['POST'])
def run_script_endpoint():
    data = request.get_json()
    script_name = data.get('script_name')
    if not script_name:
        return jsonify({"error": "script_name is required"}), 400
    run_script(script_name)
    return jsonify({"message": f"Script {script_name} executed"}), 200


# 학습 실행 함수
def run_train_model():
    try:
        logging.info("Running scheduled training...")
        base_directory = "/Tmap_project_Data/combined/"
        train_model(base_directory)
        logging.info("Scheduled training completed successfully.")
    except Exception as e:
        logging.error(f"Scheduled training failed: {e}")

# 하위 디렉토리 포함 모든 .csv 파일 검색
def get_all_csv_files(base_directory):
    return glob.glob(os.path.join(base_directory, "**", "*.csv"), recursive=True)


# ARIMA 모델 학습 함수
def train_model(base_directory, model_directory="/Tmap_project_Data/models"):
    try:
        csv_files = glob.glob(os.path.join(base_directory, "**", "*.csv"), recursive=True)
        logging.info(f"Found CSV files: {csv_files}")
        if not csv_files:
            raise FileNotFoundError(f"No CSV files found in directory {base_directory}")

        data_list = []
        for file in csv_files:
            try:
                df = pd.read_csv(file)
                df_selected = df[['PKLT_CD', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM']].copy()
                df_selected['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(df_selected['NOW_PRK_VHCL_UPDT_TM'])
                df_selected.dropna(subset=['NOW_PRK_VHCL_CNT'], inplace=True)  # 결측값 제거
                data_list.append(df_selected)
            except Exception as e:
                logging.error(f"Error reading file {file}: {e}")
                continue

        if not data_list:
            raise ValueError("No valid data found for training.")

        df_combined = pd.concat(data_list, ignore_index=True)
        df_combined = df_combined.sort_values('NOW_PRK_VHCL_UPDT_TM')
        df_combined.set_index('NOW_PRK_VHCL_UPDT_TM', inplace=True)

        os.makedirs(model_directory, exist_ok=True)

        models = {}
        for pklt_cd, group in df_combined.groupby('PKLT_CD'):
            # 데이터 검증 제거: 데이터 부족 시에도 학습 시도
            try:
                model = ARIMA(group['NOW_PRK_VHCL_CNT'], order=(5, 1, 0))
                model_fit = model.fit()
                models[pklt_cd] = model_fit
                logging.info(f"Trained ARIMA model for parking lot {pklt_cd}")
            except Exception as e:
                logging.error(f"Error training ARIMA model for {pklt_cd}: {e}")
                continue

        if not models:
            raise ValueError("No models were trained.")

        date_str = datetime.now().strftime('%Y%m%d')
        model_filepath = os.path.join(model_directory, f'arima_parking_model_{date_str}.pkl')
        with open(model_filepath, 'wb') as file:
            pickle.dump(models, file)

        logging.info(f"Model training completed. Models saved at: {model_filepath}")
        return model_filepath
    except Exception as e:
        logging.error(f"Training failed: {e}")
        raise


@app.route('/train_model', methods=['POST'])
def train_model_endpoint():
    try:
        base_directory = request.json.get('base_directory', r"C:\Tmap_project_Data\combined")
        base_directory = os.path.abspath(base_directory)  # 절대 경로로 변환

        print(f"Checking directory: {base_directory}")
        if not os.path.exists(base_directory):
            print("Directory does not exist!")
            logging.error(f"Provided directory does not exist: {base_directory}")
            return jsonify({"error": f"Provided directory does not exist: {base_directory}"}), 400
        else:
            print("Directory exists!")
            files = get_all_csv_files(base_directory)
            print(f"Found files: {files}")
            if not files:
                logging.error(f"No CSV files found in directory: {base_directory}")
                return jsonify({"error": f"No CSV files found in directory: {base_directory}"}), 400

        model_filepath = train_model(base_directory)
        return jsonify({"message": "Model trained and saved successfully", "model_path": model_filepath}), 200
    except Exception as e:
        logging.error(f"Error during training: {e}")
        return jsonify({"error": str(e)}), 500

def predict_parking(models, parking_code, df_combined, steps):
    # 모델 확인
    if parking_code not in models:
        return {"error": f"No model found for parking code '{parking_code}'"}

    # 예측 데이터 준비
    if parking_code not in df_combined['PKLT_CD'].values:
        return {"error": f"No data found for parking code '{parking_code}'"}

    # 해당 주차장 코드의 데이터 필터링
    df_specific = df_combined[df_combined['PKLT_CD'] == parking_code]

    # 예측
    model_fit = models[parking_code]
    forecast = model_fit.forecast(steps=steps)
    predicted_value = int(forecast[-1])  # 마지막 예측 값

    return {
        'parking_code': parking_code,
        'predicted_parking_count': predicted_value
    }


@app.route('/predict', methods=['POST'])
def predict_endpoint():
    try:
        data = request.get_json()

        # 파라미터 추출
        parking_code = data.get('pklt_cd')
        target_time = data.get('target_time')  # 예측 대상 시간 (ISO 8601 형식)

        if not parking_code:
            return jsonify({"error": "parking_code parameter is required"}), 400
        if not target_time:
            return jsonify({"error": "target_time parameter is required"}), 400

        # 시간 파싱
        try:
            target_time = pd.to_datetime(target_time)
        except Exception:
            return jsonify({"error": "Invalid target_time format. Use ISO 8601 format (e.g., 2024-11-20T12:00:00)"}), 400

        # 모델 로드
        model_directory = "/Tmap_project_Data/models"
        latest_model_file = sorted(glob.glob(os.path.join(model_directory, 'arima_parking_model_*.pkl')))[-1]
        logging.info(f"Loading model from file: {latest_model_file}")
        with open(latest_model_file, 'rb') as file:
            models = pickle.load(file)

        # 데이터 로드 및 정렬
        base_directory = "/Tmap_project_Data/combined/"
        csv_files = glob.glob(os.path.join(base_directory, "**", "*.csv"), recursive=True)
        data_list = []
        for file in csv_files:
            try:
                df = pd.read_csv(file)
                if 'PKLT_CD' not in df or 'NOW_PRK_VHCL_CNT' not in df or 'NOW_PRK_VHCL_UPDT_TM' not in df:
                    logging.warning(f"File {file} is missing required columns.")
                    continue

                df_selected = df[['PKLT_CD', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM']].copy()
                df_selected['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(df_selected['NOW_PRK_VHCL_UPDT_TM'], errors='coerce')
                df_selected.dropna(subset=['NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM'], inplace=True)
                data_list.append(df_selected)
            except Exception as e:
                logging.error(f"Error reading file {file}: {e}")
                continue

        if not data_list:
            raise ValueError("No valid data found for prediction. Please check your input files.")

        df_combined = pd.concat(data_list, ignore_index=True)
        df_combined = df_combined.sort_values('NOW_PRK_VHCL_UPDT_TM')

        # 현재 시간과 대상 시간 차이 계산
        now = pd.Timestamp.now()
        steps = max(1, int((target_time - now).total_seconds() / 3600))  # 시간 단위로 steps 계산

        # 예측 수행
        result = predict_parking(models, parking_code, df_combined, steps)
        result['date'] = now.strftime('%Y-%m-%d %H:%M:%S')
        result['target_time'] = target_time.strftime('%Y-%m-%d %H:%M:%S')
        return jsonify(result)
    except Exception as e:
        logging.error(f"Error during prediction: {e}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    scheduler_thread = threading.Thread(target=start_scheduler, daemon=True)
    scheduler_thread.start()
    app.run(host='0.0.0.0', port=8241, debug=False) # 스케쥴러와 debug 모드가 충돌날 것을 대비하여 debug false, 별도 쓰레드 동작으로 변경
