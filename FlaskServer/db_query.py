import psycopg2
from flask import jsonify
import jwt
from functools import wraps
from datetime import timedelta
from datetime import datetime
from flask import Flask, request, jsonify

# Flask 비밀 키 설정
SECRET_KEY = "tlqkf" 


# JWT 발급 함수
def create_jwt(payload, expiration_minutes=120):
    try:
        payload['exp'] = datetime.utcnow() + timedelta(minutes=expiration_minutes)  # timedelta 정의 문제 해결
        token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
        print(f"JWT 생성 성공: {token}")
        return token
    except Exception as e:
        print(f"JWT 생성 실패: {e}")
        raise

# JWT 검증 함수
def verify_jwt(token):
    try:
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        print(f"JWT 검증 성공: {decoded_token}")
        return decoded_token
    except jwt.ExpiredSignatureError:
        print("JWT 만료")
        return {"error": "Token expired"}
    except jwt.InvalidTokenError as e:
        print(f"JWT 유효하지 않음: {e}")
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

# PostgreSQL 데이터베이스 연결 함수
def dbConnection():
    conn = psycopg2.connect(
        host="localhost",
        port="5432",
        database="postgres",
        user="postgres",
        password="gurtn123"
    )
    return conn



# 아이디와 비밀번호를 검증하는 함수
def checkLogin(id, password):
    try:
        conn = dbConnection()
        cursor = conn.cursor()
        query = "SELECT * FROM user_info WHERE id = %s AND password = %s"
        cursor.execute(query, (id, password))
        user = cursor.fetchone()
        print(f"checkLogin result for id={id}: {user}")  # 추가 로그
        conn.close()
        return True
    except Exception as e:
        print(f"Error during checkLogin: {e}")
        return False


#아이디 찾기
def findUserId(name, phone):
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT id FROM user_info WHERE name = %s AND user_tel = %s"
    cursor.execute(query, (name, phone))
    user = cursor.fetchone()
    conn.close()

    return user[0] if user else None

def findPassword(name, user_id, user_tel):
    conn = dbConnection()
    cursor = conn.cursor()

    query = "SELECT password FROM user_info WHERE name = %s AND id = %s AND user_tel = %s"
    cursor.execute(query, (name, user_id, user_tel))
    user = cursor.fetchone()  # 튜플로 반환됨
    conn.close()

    # 튜플에서 첫 번째 값(비밀번호)을 반환하거나, 없으면 None 반환
    return user[0] if user else None


# 특정 주차장의 현재 주차량 조회
def getUserCustomInfo():
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT * FROM user_custom_info"
    cursor.execute(query)
    result = cursor.fetchone()
    conn.close()
    return {'user_custom_info': result[0] if result else 'No data found'}

#회원가입
def register_user(id, password, name, user_tel, birthday):
    conn = dbConnection()
    cursor = conn.cursor()
    try:
        query = """
        INSERT INTO user_info (id, password, name, user_tel, birthday)
        VALUES (%s, %s, %s, %s, %s)
        """
        cursor.execute(query, (id, password, name, user_tel, birthday))
        conn.commit()
        return True
    except Exception as e:
        print("Error:", e)
        conn.rollback()
        return False
    finally:
        conn.close()
        
# 특정 날짜의 주차량 평균 조회
def get_average_parking_by_date(date):
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT AVG(CUR_PARKING) as avg_parking FROM parking_data WHERE DATE(CUR_PARKING_TIME) = %s"
    cursor.execute(query, (date,))
    result = cursor.fetchone()
    conn.close()
    return {'average_parking': result[0] if result else 'No data found'}

# 주차장별 최대 주차량 조회
def get_max_parking_per_location():
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT PARKING_NAME, MAX(CUR_PARKING) as max_parking FROM parking_data GROUP BY PARKING_NAME"
    cursor.execute(query)
    result = cursor.fetchall()
    conn.close()
    return [{'PARKING_NAME': row[0], 'max_parking': row[1]} for row in result]

# 특정 시간대 주차 정보 조회
def get_parking_by_time_range(start_time, end_time):
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT * FROM parking_data WHERE CUR_PARKING_TIME BETWEEN %s AND %s"
    cursor.execute(query, (start_time, end_time))
    result = cursor.fetchall()
    conn.close()
    return [dict(zip([desc[0] for desc in cursor.description], row)) for row in result]

# 사용자 요청 위도, 사용자 요청 경도 , 사용자 요청 위도 순
# 원하는 목적지 위도 경도에 가까운 순으로 5개 조회
def getParkInfo(u_lat, u_lot):
    # 위도와 경도를 double(float) 타입으로 변환
    u_lat = float(u_lat)
    u_lot = float(u_lot)

    conn = dbConnection()
    cursor = conn.cursor()
    query = ("SELECT pklt_nm, lat, lot, ROUND(6371000 * acos(cos(radians(%s)) * cos(radians(lat)) * cos(radians(lot) - radians(%s)) + sin(radians(%s)) * sin(radians(lat)))) AS distance "
             "FROM parking_info pi2 "
             "ORDER BY distance LIMIT 5")
    cursor.execute(query, (u_lat, u_lot, u_lat))
    result = cursor.fetchall()
    conn.close()

    parking_list = [
        {"name": row[0], "latitude": row[1], "longitude": row[2], "distance": row[3]}
        for row in result
    ]
    return parking_list