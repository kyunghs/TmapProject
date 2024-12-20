# api 제어 및 스크립트 제어 중앙컨트롤기 = 넥서스
# made 경혁수
# author 이민호
import sys
import random
import pandas as pd
import glob
import os
import re
import pickle
from flask import Flask, request, jsonify
from statsmodels.tsa.arima.model import ARIMA
from apscheduler.schedulers.background import BackgroundScheduler
import threading
import subprocess
import time
import logging
import db_query
from concurrent.futures import ThreadPoolExecutor
import jwt
from functools import wraps
from datetime import datetime, timedelta

# Flask 비밀 키 설정
SECRET_KEY = "tlqkf" 

# 로그 설정
logging.basicConfig(level=logging.INFO)
logging.getLogger('apscheduler').setLevel(logging.DEBUG)

app = Flask(__name__)

# JWT 발급 함수
def create_jwt(payload, expiration_minutes=120):
    payload['exp'] = datetime.utcnow() + timedelta(minutes=expiration_minutes)
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")

# JWT 검증 함수
def verify_jwt(token):
    try:
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        return decoded_token
    except jwt.ExpiredSignatureError:
        return {"error": "Token expired"}
    except jwt.InvalidTokenError:
        return {"error": "Invalid token"}

# JWT 보호를 위한 데코레이터
def jwt_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers.get("Authorization")
        if not token:
            return jsonify({"success": False, "message": "토큰이 없습니다."}), 401
        try:
            token = token.split(" ")[1]  # Bearer <token>
            decoded_token = verify_jwt(token)
            if "error" in decoded_token:
                return jsonify({"success": False, "message": decoded_token["error"]}), 401
        except Exception as e:
            return jsonify({"success": False, "message": "유효하지 않은 요청"}), 401
        return f(*args, **kwargs, user=decoded_token)
    return decorated_function

#GIT에서 소스가 push될 때마다 WEBHOOK을 이용해 자동으로 PULL 받고 서버 RELOAD
@app.route('/webhook', methods=['POST'])
def git_webhook():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data received"}), 400

        # Webhook 데이터 로그 출력 (디버깅용)
        logging.info(f"Received Webhook data: {data}")

        # Push 이벤트 처리
        if 'ref' in data and 'refs/heads/main' in data['ref']:
            logging.info("Push event detected. Pulling changes...")

            # Git Pull 명령 실행
            subprocess.run(['git', 'pull', 'origin', 'main'], cwd=os.path.dirname(os.path.abspath(__file__)))

            # 서버 리로드 (현재 Flask 프로세스를 재시작)
            logging.info("Restarting Flask server...")
            os.execv(__file__, ['python'] + sys.argv)

        return jsonify({"message": "Webhook processed successfully"}), 200
    except Exception as e:
        logging.error(f"Error processing webhook: {e}")
        return jsonify({"error": str(e)}), 500


# 스크립트 실행 함수 (수정된 부분)
# def run_script(script_name):
#     try:
#         script_path = os.path.abspath(os.path.join("scripts", script_name))
#         if not os.path.isfile(script_path):
#             raise FileNotFoundError(f"Script {script_name} not found at {script_path}")

#         # 스크립트 실행
#         result = subprocess.run(
#             ["python", script_path], capture_output=True, text=True
#         )

#         # 로그 기록
#         logging.info(f"Running script: {script_name}")
#         logging.info(f"Output: {result.stdout}")

#         # 에러 처리
#         if result.stderr:
#             logging.error(f"Error in script {script_name}: {result.stderr}")
#             return {"success": False, "error": result.stderr.strip()}

#         # 성공 응답
#         return {"success": True, "output": result.stdout.strip()}
#     except Exception as e:
#         logging.error(f"Exception while running script {script_name}: {e}")
#         return {"success": False, "error": str(e)}

# # 스케줄러 초기화
# scheduler = BackgroundScheduler()

# # Delete 실행: 매 5분마다 실행
# scheduler.add_job(func=lambda: run_script("Delete_parking_info.py"),
#                   trigger="cron", minute="*/5", id="delete_parking_info")
# # 커밋 테스트 이거야
# # 커밋 테스트 이거야
# # 커밋 테스트 이거야
# # TIME.SLEEP 사용 시 충돌 문제 발생하여 비동기로 처리
# executor = ThreadPoolExecutor()

# # Request 1 실행: Delete 실행 후 10초 대기 후 실행
# scheduler.add_job(func=lambda: executor.submit(run_script, "Request_parking_filtered_1.py"),
#                   trigger="cron", minute="*/5", id="request_parking_1")

# # Request 2 실행: Request 1 실행 후 10초 대기 후 실행
# scheduler.add_job(func=lambda: executor.submit(run_script, "Request_parking_filtered_2.py"),
#                   trigger="cron", minute="*/5", id="request_parking_2")

# # 자정에 all_filtered 실행
# scheduler.add_job(func=lambda: run_script("all_filterd.py"),
#                   trigger="cron", hour=0, minute=0, id="all_filtered")

# # 자정에 모델 학습
# # scheduler.add_job(func=lambda: run_train_model(),
# #                   trigger="cron", hour=1, minute=10, id="train_model_scheduler")

# # 스케줄러 스레드 시작
# def start_scheduler():
#     scheduler.start()

# # Flask 엔드포인트
# @app.route('/', methods=['GET'])
# def index():
#     return "Index Page - Parking Scheduler"

# # Flask 엔드포인트에서 스크립트 실행 함수 호출 시 수정
# @app.route('/run_script', methods=['POST'])
# def run_script_endpoint():
#     data = request.get_json()
#     script_name = data.get('script_name')
#     if not script_name:
#         return jsonify({"success": False, "message": "script_name is required"}), 400

#     # 결과 반환
#     result = run_script(script_name)
#     if result.get("success"):
#         return jsonify({"message": f"Script {script_name} executed successfully", "output": result.get("output")}), 200
#     else:
#         return jsonify({"error": result.get("error")}), 500


@app.route('/get/park/info', methods=['POST'])
def getParkInfo():
    try:
        data = request.json
        print("Received data:", data)
        lat = data.get('lat')
        lot = data.get('lot')
        print(f"Latitude: {lat}, Longitude: {lot}")

        # Your DB query logic
        parks = db_query.getParkInfo(lat, lot)
        if parks:
            return jsonify({"parks": parks}), 200
        else:
            return jsonify({"error": "잘못된 주차장 요청입니다."}), 404
    except Exception as e:
        print("Error:", e)
        return jsonify({"error": "서버 오류"}), 500


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

@app.route('/Login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"success": False, "message": "요청 데이터가 비어있습니다."}), 400

        id = data.get('id')
        password = data.get('password')

        if not id or not password:
            return jsonify({"success": False, "message": "아이디와 비밀번호를 모두 입력하세요."}), 400

        # 로그인 검증
        is_valid = db_query.checkLogin(id, password)  # 올바른 검증 로직 호출
        print(f"Login query result for id={id}: {is_valid}")  # 디버깅 로그

        if is_valid:
            token = create_jwt({"id": id})
            return jsonify({"success": True, "message": "로그인 성공", "token": token}), 200
        else:
            return jsonify({"success": False, "message": "아이디 또는 비밀번호가 잘못되었습니다."}), 401
    except Exception as e:
        print(f"Error in /Login endpoint: {e}")
        return jsonify({"success": False, "message": "서버 오류 발생"}), 500




@app.route('/findUserId', methods=['POST'])
def find_user_id():
    try:
        data = request.get_json()
        name = data.get('name')
        phone = data.get('phone')

        if not name or not phone:
            return jsonify({"success": False, "message": "이름과 전화번호를 모두 입력하세요."}), 400

        user_id = db_query.findUserId(name, phone)
        if user_id:
            return jsonify({"success": True, "userId": user_id}), 200
        else:
            return jsonify({"success": False, "message": "조건에 맞는 사용자를 찾을 수 없습니다."}), 404
    except Exception as e:
        return jsonify({"success": False, "message": "서버 오류: " + str(e)}), 500
    
@app.route('/findUserPw', methods=['POST'])
def find_password():
    data = request.get_json()
    name = data.get('name')  # POST 데이터에서 name 가져오기
    user_id = data.get('id')  # POST 데이터에서 id 가져오기
    user_tel = data.get('user_tel')  # POST 데이터에서 user_tel 가져오기

    # 필수 입력 데이터 검증
    if not (name and user_id and user_tel):
        return jsonify({"success": False, "message": "필수 항목을 모두 입력하세요."}), 400

    # 비밀번호 찾기 함수 호출
    user_password = db_query.findPassword(name, user_id, user_tel)
    if user_password:
        return jsonify({"success": True, "password": user_password})
    else:
        return jsonify({"success": False, "message": "사용자를 찾을 수 없습니다."}), 404


@app.route('/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        id = data.get('id')
        password = data.get('password')
        name = data.get('name')
        user_tel = data.get('user_tel')
        birthday = data.get('birthday')

        if not (id and password and name and user_tel and birthday):
            return jsonify({"success": False, "message": "모든 필드를 입력하세요."}), 400

        success = db_query.register_user(id, password, name, user_tel, birthday)

        if success:
            return jsonify({"success": True, "message": "회원가입 성공!"}), 200
        else:
            return jsonify({"success": False, "message": "회원가입 실패. 서버 오류 발생."}), 500
    except Exception as e:
        print("Error in /register endpoint:", e)
        return jsonify({"success": False, "message": f"서버 오류: {str(e)}"}), 500


# 사용자 정보 가져오기
@app.route('/getUserInfo', methods=['GET'])
@jwt_required
def get_user_info(user=None):  # JWT 데코레이터로부터 전달된 사용자 정보
    try:
        print(f"JWT 디코딩 결과: {user}")  # 디버깅 로그 추가

        if not user or 'id' not in user:
            return jsonify({"success": False, "message": "유효하지 않은 사용자 정보입니다."}), 400

        user_id = user['id']
        user_data = db_query.get_user_info_by_id(user_id)
        print(f"DB 조회 결과: {user_data}")  # 디버깅 로그 추가

        if user_data:
            # 선택 상태 포함 데이터 반환
            return jsonify({
                "success": True,
                "data": {
                    "name": user_data["name"],
                    "id": user_data["id"],
                    "disabled_human": user_data["disabled_human"],
                    "multiple_child": user_data["multiple_child"],
                    "electric_car": user_data["electric_car"],
                    "person_merit": user_data["person_merit"],
                    "tax_payment": user_data["tax_payment"],
                    "alone_family": user_data["alone_family"]
                }
            }), 200
        else:
            return jsonify({"success": False, "message": "사용자 데이터를 찾을 수 없습니다."}), 404
    except Exception as e:
        print(f"서버 오류: {e}")
        return jsonify({"success": False, "message": "서버 오류가 발생했습니다."}), 500

# 수정할 회원정보 가져오기 
@app.route('/getEditUserInfo', methods=['GET'])
@jwt_required
def get_edit_user_info(user):
    try:
        # JWT에서 사용자 ID 가져오기
        user_id = user.get("id")
        if not user_id:
            return jsonify({"message": "유효하지 않은 사용자 정보입니다.", "success": False}), 400

        # DB에서 사용자 정보 조회
        user_data = db_query.get_edit_user_info_by_id(user_id)

        if user_data:
            return jsonify({
                "message": "회원 정보를 성공적으로 가져왔습니다.",
                "success": True,
                "data": user_data
            }), 200
        else:
            return jsonify({"message": "사용자를 찾을 수 없습니다.", "success": False}), 404
    except Exception as e:
        print(f"Error in get_edit_user_info: {e}")
        return jsonify({"message": "서버 오류가 발생했습니다.", "success": False}), 500

# 대시보드 정보 가져오기 
@app.route('/main/dashboard', methods=['POST'])
@jwt_required
def get_main_info(user):
    try:
        # JWT에서 사용자 ID 가져오기
        user_id = user.get("id")
        if not user_id:
            return jsonify({"message": "유효하지 않은 사용자 정보입니다.", "success": False}), 400

        # DB에서 사용자 정보 조회
        user_data = db_query.get_edit_user_info_by_id(user_id)

        if user_data:
            # 대시보드 작업
            user_custom_data = db_query.get_user_custom_info(user_data["user_code"])
            get_user_history_data = db_query.get_user_history(user_data["user_code"])
            get_user_ki_history_data = db_query.get_user_ki_history(user_data["user_code"])

            return jsonify({
                "message": "대시보드 정보를 성공적으로 가져왔습니다.",
                "success": True,
                "data": {
                    "user_custom_data": user_custom_data,
                    "get_user_history_data":get_user_history_data,
                    "get_user_ki_history_data":get_user_ki_history_data,
                }
            }), 200
        else:
            return jsonify({"message": "사용자를 찾을 수 없습니다.", "success": False}), 404
    except Exception as e:
        print(f"Error in get_edit_user_info: {e}")
        return jsonify({"message": "서버 오류가 발생했습니다.", "success": False}), 500


# 대시보드 정보 가져오기 
@app.route('/update_user_custom_data', methods=['POST'])
@jwt_required
def update_user_custom_data(user):
    try:
        # JWT에서 사용자 ID 가져오기

        body = request.get_json()
        if not body:
            return jsonify({"error": "No data provided"}), 400

        user_id = user.get("id")
        if not user_id:
            return jsonify({"message": "유효하지 않은 사용자 정보입니다.", "success": False}), 400

        # DB에서 사용자 정보 조회

        area = body.get("area", "")
        area_alias = body.get("area_alias", "")
        area_address = body.get("area_address", "")

        user_data = db_query.get_edit_user_info_by_id(user_id)
        if user_data:
            # 대시보드 작업
            category = body.get("category", 1)

            if category == 1:
                db_query.update_user_custom_1(user_data["user_code"], {"area": area, "area_alias":area_alias, "area_address": area_address })
            else:
                db_query.update_user_custom_2(user_data["user_code"], {"area": area, "area_alias":area_alias, "area_address": area_address })
        
            return jsonify({
                "message": "완료",
                "success": True,
            }), 200
        else:
            return jsonify({"message": "사용자를 찾을 수 없습니다.", "success": False}), 404
       
    
    except Exception as e:
        print(f"Error in get_edit_user_info: {e}")
        return jsonify({"message": "서버 오류가 발생했습니다.", "success": False}), 500



@app.route('/updateUserInfo', methods=['POST'])
def update_user_info():
    try:
        data = dict(request.json)
        
        # 요청 데이터에서 ID 가져오기
        user_id = data.get('id')
        if not user_id:
            return jsonify({"message": "Invalid user ID", "success": False}), 400

        # 필수 필드 유효성 검사
        if not all(key in data for key in ['name', 'user_tel', 'password']):
            return jsonify({"message": "Missing required fields", "success": False}), 400

        # 유저 정보 업데이트
        success = db_query.update_user_info(user_id, data)
        if success:
            return jsonify({"message": "User information updated successfully", "success": True}), 200
        else:
            return jsonify({"message": "Update failed", "success": False}), 400
    except Exception as e:
        print(f"Error in update_user_info: {e}")
        return jsonify({"message": "Server error", "success": False}), 500


@app.route('/updateUserSelection', methods=['POST'])
def update_user_selection():
    """
    사용자 선택 항목 업데이트 API
    """
    try:
        # 요청 데이터 파싱
        data = request.get_json()
        print(f"요청받은 데이터: {data}")  # 디버깅 로그

        # 요청 데이터 검증
        user_id = 'ky'  # 테스트용 사용자 ID (하드코딩으로 확인)
        selected_column = data.get("selected_column")  # 선택된 항목 (DB 컬럼명)
        print(f"사용자 ID: {user_id}, 선택된 컬럼: {selected_column}")  # 디버깅 로그
        
        if not user_id or not selected_column:
            return jsonify({"success": False, "message": "사용자 ID 또는 선택 항목이 없습니다."}), 400

        # 유효한 컬럼인지 확인
        valid_columns = ["disabled_human", "multiple_child", "electric_car", "person_merit", "tax_payment", "alone_family"]
        if selected_column not in valid_columns:
            print(f"유효하지 않은 컬럼: {selected_column}")  # 디버깅 로그
            return jsonify({"success": False, "message": f"'{selected_column}'는 유효하지 않은 컬럼입니다."}), 400

        # DB 업데이트 시도
        success = db_query.update_user_selection(user_id, selected_column, valid_columns)
        print(f"DB 업데이트 성공 여부: {success}")  # 디버깅 로그

        if success:
            return jsonify({"success": True, "message": "사용자 선택이 업데이트되었습니다."}), 200
        else:
            return jsonify({"success": False, "message": "DB 업데이트에 실패했습니다."}), 500
    except Exception as e:
        print(f"서버 오류: {e}")  # 디버깅 로그
        return jsonify({"success": False, "message": "서버 오류가 발생했습니다."}), 500






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


# 모델 로드
MODEL_PATH = "arima_parking_model_2.pkl"

def load_model(model_path):
    
    try:
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model file not found at {model_path}")

        with open(model_path, 'rb') as file:
            model = pickle.load(file)
        logging.info("Model loaded successfully.")
        return model
    except Exception as e:
        logging.error(f"Error loading model: {e}")
        raise

# 예측
@app.route('/predict', methods=['POST'])
def predict_endpoint():
    try:
        # JSON 데이터 파싱
        data = request.get_json()

        # 요청 데이터가 배열인지 확인
        if isinstance(data, list):
            # 요청이 배열일 경우
            results = []
            for item in data:
                parking_code = item.get('pklt_cd')
                if not parking_code:
                    return jsonify({"error": "parking_code parameter is required for one of the items"}), 400

                # 랜덤 예측 값 생성
                prediction = str(random.randint(8, 40))

                # 결과 추가
                results.append({
                    "pklt_cd": parking_code,
                    "predicted_value": prediction
                })
            
            
            # 배열로 응답
            return jsonify(results), 200
        else:
            # 요청이 단일 객체일 경우
            parking_code = data.get('pklt_cd')
            if not parking_code:
                return jsonify({"error": "parking_code parameter is required"}), 400

            # 랜덤 예측 값 생성
            prediction = str(random.randint(8, 40))

            return jsonify({
                "pklt_cd": parking_code,
                "predicted_value": prediction
            }), 200

    except Exception as e:
        logging.error(f"Error during prediction: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/selectPark', methods=['POST'])
def selectPark():
    try:
        parkList = db_query.selectPark()
        return jsonify({"success": True, "parkList": parkList}), 200
    except Exception as e:
        return jsonify({"success": False, "message": "서버 오류: " + str(e)}), 500


@app.route('/insertHistory', methods=['POST'])
def insertHistory():
    try:
        data = request.get_json()
        name = data.get('name')
        departure = data.get('departure')
        destination = data.get('destination')
        destination_address = data.get('destination_address')
        kilometers = data.get('kilometers')

        # 데이터 삽입 쿼리 실행
        success = db_query.insertHistory(name, departure, destination, destination_address, kilometers)

        if success:
            return jsonify({"success": True}), 200
        else:
            return jsonify({"success": False, "message": "데이터 삽입 실패"}), 500
    except Exception as e:
        return jsonify({"success": False, "message": "서버 오류: " + str(e)}), 500

# 이전 예측 소스



# @app.route('/predict', methods=['POST'])
# def predict_endpoint():
#     try:
#         data = request.get_json()

#         # 파라미터 추출
#         parking_code = data.get('pklt_cd')
#         target_time = data.get('target_time')  # 예측 대상 시간 (ISO 8601 형식)

#         if not parking_code:
#             return jsonify({"error": "parking_code parameter is required"}), 400
#         if not target_time:
#             return jsonify({"error": "target_time parameter is required"}), 400

#         # 시간 파싱
#         try:
#             target_time = pd.to_datetime(target_time)
#         except Exception:
#             return jsonify({"error": "Invalid target_time format. Use ISO 8601 format (e.g., 2024-11-20T12:00:00)"}), 400

#         # 모델 로드
#         model_directory = "/Tmap_project_Data/models"
#         latest_model_file = sorted(glob.glob(os.path.join(model_directory, 'arima_parking_model_*.pkl')))[-1]
#         logging.info(f"Loading model from file: {latest_model_file}")
#         with open(latest_model_file, 'rb') as file:
#             models = pickle.load(file)

#         # 데이터 로드 및 정렬
#         base_directory = "/Tmap_project_Data/combined/"
#         csv_files = glob.glob(os.path.join(base_directory, "**", "*.csv"), recursive=True)
#         data_list = []
#         for file in csv_files:
#             try:
#                 df = pd.read_csv(file)
#                 if 'PKLT_CD' not in df or 'NOW_PRK_VHCL_CNT' not in df or 'NOW_PRK_VHCL_UPDT_TM' not in df:
#                     logging.warning(f"File {file} is missing required columns.")
#                     continue

#                 df_selected = df[['PKLT_CD', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM']].copy()
#                 df_selected['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(df_selected['NOW_PRK_VHCL_UPDT_TM'], errors='coerce')
#                 df_selected.dropna(subset=['NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM'], inplace=True)
#                 data_list.append(df_selected)
#             except Exception as e:
#                 logging.error(f"Error reading file {file}: {e}")
#                 continue

#         if not data_list:
#             raise ValueError("No valid data found for prediction. Please check your input files.")

#         df_combined = pd.concat(data_list, ignore_index=True)
#         df_combined = df_combined.sort_values('NOW_PRK_VHCL_UPDT_TM')

#         # 현재 시간과 대상 시간 차이 계산
#         now = pd.Timestamp.now()
#         steps = max(1, int((target_time - now).total_seconds() / 3600))  # 시간 단위로 steps 계산

#         # 예측 수행
#         result = predict_parking(models, parking_code, df_combined, steps)
#         result['date'] = now.strftime('%Y-%m-%d %H:%M:%S')
#         result['target_time'] = target_time.strftime('%Y-%m-%d %H:%M:%S')
#         return jsonify(result)
#     except Exception as e:
#         logging.error(f"Error during prediction: {e}")
#         return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    """ if not os.getenv("WERKZEUG_RUN_MAIN"):  # Flask 재시작 감지
        if not scheduler.running:
            logging.info("Starting BackgroundScheduler...")
            scheduler_thread = threading.Thread(target=start_scheduler, daemon=True)
            scheduler_thread.start() """
    app.run(host='0.0.0.0', port=8241, debug=True) # 스케쥴러와 debug 모드가 충돌날 것을 대비하여 debug false, 별도 쓰레드 동작으로 변경