import psycopg2
from flask import jsonify

# PostgreSQL 데이터베이스 연결 함수
def dbConnection():
    conn = psycopg2.connect(
        host="220.116.209.226",
        port="5432",
        database="postgres",
        user="postgres",
        password="gurtn123"
    )
    return conn


# 아이디와 비밀번호를 검증하는 함수
def checkLogin(id, password):
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT * FROM user_info WHERE id = %s AND password = %s"
    cursor.execute(query, (id, password))
    user = cursor.fetchone()
    conn.close()

    # 사용자가 있으면 True, 없으면 False 반환
    return user is not None

#아이디 찾기
def findUserId(name, user_tel, birthday):
    conn = db_connection()
    cursor = conn.cursor()
    query = "SELECT id FROM user_info WHERE name = %s AND user_tel = %s AND birthday = %s"
    cursor.execute(query, (name, user_tel, birthday))
    user = cursor.fetchone()
    conn.close()

    # 조건에 맞는 사용자가 있으면 ID 반환, 없으면 None 반환
    return user['ID'] if user else None

#비밀번호 찾기
def findPassword(name, id, user_tel):
    conn = db_connection()
    cursor = conn.cursor()
    query = "SELECT password FROM user_info WHERE name = %s AND id = %s AND user_tel = %s"
    cursor.execute(query, (name, id, user_tel))
    user = cursor.fetchone()
    conn.close()

    # 조건에 맞는 사용자가 있으면 비밀번호 반환, 없으면 None 반환
    return user['PASSWORD'] if user else None

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
    conn = db_connection()
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
def get_parking_by_location(u_lat, u_lot, u_lat_2):
    conn = dbConnection()
    cursor = conn.cursor()
    query = "SELECT pklt_nm, lat, lot,ROUND(6371000 * acos(cos(radians(%s)) * cos(radians(lat)) * cos(radians(lot) - radians(%s)) + sin(radians(%s)) * sin(radians(lat)))) AS distance FROM parking_info pi2 ORDER BY distance LMIT 5"
    cursor.execute(query, (u_lat,u_lot))
    result = cursor.fetchall()
    conn.close()

    parking_list = [
        {"name": row[0], "latitude": row[1], "longitude": row[2], "distance": row[3]}
        for row in result
    ]
    return parking_list
