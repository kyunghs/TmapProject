import pandas as pd
import glob
import os
import re
import pickle
from datetime import datetime
from flask import Flask, request, jsonify
from statsmodels.tsa.arima.model import ARIMA

import db_query

app = Flask(__name__)

# CSV 파일 전체 데이터를 준비하는 함수
def prepare_data(base_directory):
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
    return df_combined


def train_model(df_combined, model_directory="models"):
    # 모델 저장 디렉토리가 없다면 생성
    os.makedirs(model_directory, exist_ok=True)

    # 디렉토리 내에서 가장 높은 시퀀스 번호 찾기
    existing_files = glob.glob(os.path.join(model_directory, 'arima_parking_model_*.pkl'))
    max_sequence = 0
    for file in existing_files:
        match = re.search(r'arima_parking_model_(\d+)\.pkl', file)
        if match:
            sequence_num = int(match.group(1))
            max_sequence = max(max_sequence, sequence_num)

    # 다음 파일명 시퀀스 설정
    next_sequence = max_sequence + 1
    model_filename = f'arima_parking_model_{next_sequence}.pkl'
    model_filepath = os.path.join(model_directory, model_filename)

    # ARIMA 모델 학습 및 저장
    model = ARIMA(df_combined['CUR_PARKING'], order=(5, 1, 0))
    model_fit = model.fit()
    with open(model_filepath, 'wb') as file:
        pickle.dump(model_fit, file)

    return model_filepath  # 저장된 모델 파일 경로 반환

# 예측을 수행하는 함수
def predict_parking(date_str, model_fit, parking_name, df_combined):
    # 특정 주차장 데이터만 필터링
    df_specific = df_combined[df_combined['PARKING_NAME'] == parking_name]
    if df_specific.empty:
        return {"error": f"No data found for parking name '{parking_name}'"}

    forecast_steps = 24
    forecast = model_fit.forecast(steps=forecast_steps)
    average_forecast = int(forecast.mean())
    result = {
        'date': date_str,
        'parking_name': parking_name,
        'predicted_cur_parking': average_forecast
    }
    return result

# 로그인 엔드포인트
@app.route('/login', methods=['POST'])
def login():
    # 요청에서 JSON 데이터 가져오기
    data = request.get_json()
    id = data.get('id')
    password = data.get('password')

    # 아이디와 비밀번호가 제공되지 않으면 오류 반환
    if not id or not password:
        return jsonify({"error": "id and password are required"}), 400

    # 아이디와 비밀번호 검증
    is_valid_user = db_query.checkLogin(id, password)
    if is_valid_user:
        return jsonify({"message": "로그인에 성공하였습니다."}), 200
    else:
        return jsonify({"error": "아이디 혹은 비밀번호가 일치하지 않습니다."}), 401


#아이디 찾기
@app.route('/find/id', methods=['POST'])
def find_id():
    # 요청에서 JSON 데이터 가져오기
    data = request.get_json()
    name = data.get('name')
    user_tel = data.get('user_tel')
    birthday = data.get('birthday')

    # 필요한 정보가 모두 제공되지 않으면 오류 반환
    if not name or not user_tel or not birthday:
        return jsonify({"error": "name, user_tel, and birthday are required"}), 400

    # 조건에 맞는 아이디 찾기
    id = db_query.findUserId(name, user_tel, birthday)
    if id:
        return jsonify({"id": id}), 200
    else:
        return jsonify({"error": "해당 정보로 등록된 아이디가 없습니다."}), 404

#비밀번호 찾기
@app.route('/find/password', methods=['POST'])
def find_password():
    # 요청에서 JSON 데이터 가져오기
    data = request.get_json()
    name = data.get('name')
    id = data.get('id')
    user_tel = data.get('user_tel')

    # 필요한 정보가 모두 제공되지 않으면 오류 반환
    if not name or not id or not user_tel:
        return jsonify({"error": "name, id, and user_tel are required"}), 400

    # 조건에 맞는 비밀번호 찾기
    password = db_query.findPassword(name, id, user_tel)
    if password:
        return jsonify({"password": password}), 200
    else:
        return jsonify({"error": "해당 정보로 등록된 비밀번호가 없습니다."}), 404

#회원가입
@app.route('/register', methods=['POST'])
def register():
    # 요청에서 JSON 데이터 가져오기
    data = request.get_json()
    id = data.get('id')
    password = data.get('password')
    name = data.get('name')
    user_tel = data.get('user_tel')
    birthday = data.get('birthday')  # 형식: 'YYYY-MM-DD'

    # 필수 정보가 모두 제공되지 않으면 오류 반환
    if not id or not password or not name or not user_tel or not birthday:
        return jsonify({"error": "All fields are required"}), 400

    # 사용자 등록
    result = db_query.register_user(id, password, name, user_tel, birthday)
    if result:
        return jsonify({"message": "사용자가 성공적으로 등록되었습니다."}), 201
    else:
        return jsonify({"error": "사용자 등록에 실패했습니다."}), 500


# 모델 학습을 요청하는 엔드포인트
@app.route('/requestModeling', methods=['GET'])
def requestModeling():
    base_directory = r"/Users/kyung/PycharmProjects/FlaskServer/수집데이터_1"
    df_combined = prepare_data(base_directory)
    train_model(df_combined)
    return jsonify({"message": "Model trained and saved successfully"}), 200

# 예측을 수행하는 엔드포인트
@app.route('/Predict', methods=['GET'])
def predict():
    date_str = request.args.get('date', default=datetime.now().strftime('%Y-%m-%d %H:%M:%S'), type=str)
    parking_name = request.args.get('parking_name', type=str)

    if not parking_name:
        return jsonify({"error": "parking_name parameter is required"}), 400

    # 저장된 모델 불러오기
    model_filename = 'arima_parking_model.pkl'
    try:
        with open(model_filename, 'rb') as file:
            loaded_model = pickle.load(file)
    except FileNotFoundError:
        return jsonify({"error": "Model not found. Please run /RequestModeling to train the model first."}), 500

    # 전체 데이터 준비
    base_directory = r"/Users/kyung/PycharmProjects/FlaskServer/수집데이터_1"
    df_combined = prepare_data(base_directory)

    # 예측 수행
    result = predict_parking(date_str, loaded_model, parking_name, df_combined)
    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8080)
