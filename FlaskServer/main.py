import sys
import pandas as pd
import glob
import os
import pickle
from datetime import datetime
from flask import Flask, request, jsonify
import subprocess
import logging
import db_query

# 로그 설정
logging.basicConfig(
    filename='app.log',
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app = Flask(__name__)

# GIT에서 소스가 push될 때마다 WEBHOOK을 이용해 자동으로 PULL 받고 서버 RELOAD
@app.route('/webhook', methods=['POST'])
def git_webhook():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data received"}), 400

        # Webhook 데이터 로그 출력
        app.logger.info(f"Received Webhook data: {data}")

        # Push 이벤트 처리
        if 'ref' in data and 'refs/heads/main' in data['ref']:
            app.logger.info("Push event detected. Pulling changes...")

            # Git Pull 명령 실행
            subprocess.run(['git', 'pull', 'origin', 'main'], cwd=os.path.dirname(os.path.abspath(__file__)))

            # Flask 서버를 재시작하도록 로깅
            app.logger.info("Code updated. Please restart the server manually in production.")

        return jsonify({"message": "Webhook processed successfully"}), 200
    except Exception as e:
        app.logger.error(f"Error processing webhook: {e}")
        return jsonify({"error": str(e)}), 500

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

@app.route('/Login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        print(f"Received data: {data}")  # 요청 데이터 확인
        id = data.get('id')
        password = data.get('password')

        if not id or not password:
            return jsonify({"success": False, "message": "아이디와 비밀번호를 모두 입력하세요."}), 400

        # 로그인 검증
        is_valid = db_query.checkLogin(id, password)
        print(f"Login query result for id={id}: {is_valid}")  # 쿼리 결과 확인

        if is_valid:
            return jsonify({"success": True, "message": "로그인 성공"}), 200
        else:
            return jsonify({"success": False, "message": "아이디 또는 비밀번호가 잘못되었습니다."}), 401
    except Exception as e:
        print(f"Error in /Login endpoint: {e}")  # 예외 로그
        return jsonify({"success": False, "message": "서버 오류 발생"}), 500

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8241, debug=True)
