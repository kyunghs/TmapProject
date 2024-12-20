import psycopg2
from flask import jsonify
import jwt
from functools import wraps
from datetime import timedelta
from datetime import datetime
from flask import Flask, request, jsonify
from datetime import datetime, timedelta

# Flask 비밀 키 설정
SECRET_KEY = "tlqkf" 


# JWT 발급 함수
def create_jwt(payload, expiration_minutes=120):
    """
    JWT 생성 함수.
    :param payload: dict - 토큰에 포함할 데이터
    :param expiration_minutes: int - 토큰의 유효 시간 (분 단위)
    :return: str - 생성된 JWT 토큰
    """
    try:
        payload['exp'] = datetime.utcnow() + timedelta(minutes=expiration_minutes)
        token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
        print(f"JWT 생성 성공: {token}")
        return token
    except jwt.PyJWTError as e:
        print(f"JWT 생성 실패: {e}")
        raise
    except Exception as e:
        print(f"Unknown error during JWT creation: {e}")
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
        except Exception:
            return jsonify({"success": False, "message": "유효하지 않은 요청"}), 401
        return f(*args, **kwargs, user=decoded_token)  # user 정보 전달
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
        conn = dbConnection()  # 데이터베이스 연결
        cursor = conn.cursor()

        query = """
        SELECT * FROM user_info WHERE id = %s AND password = %s
        """
        cursor.execute(query, (id, password))
        user = cursor.fetchone()  # 일치하는 사용자가 있는지 확인
        conn.close()

        return user is not None  # 사용자가 있으면 True, 없으면 False 반환
    except Exception as e:
        print(f"Error in checkLogin: {e}")
        return False  # 에러 발생 시 로그인 실패 처리
        

# 사용자 정보를 ID를 기반으로 가져오는 함수
def get_user_info_by_id(user_id):
    try:
        conn = dbConnection()  # DB 연결
        cursor = conn.cursor()

        query = """
        SELECT name, id, disabled_human, multiple_child, electric_car, person_merit, tax_payment, alone_family
        FROM user_info
        WHERE id = %s
        """
        cursor.execute(query, (user_id,))
        result = cursor.fetchone()

        if result:
            return {
                "name": result[0],
                "id": result[1],
                "disabled_human": result[2],
                "multiple_child": result[3],
                "electric_car": result[4],
                "person_merit": result[5],
                "tax_payment": result[6],
                "alone_family": result[7]
            }
        else:
            return None
    except Exception as e:
        print(f"DB 오류: {e}")
        return None
    finally:
        conn.close()


# 회원정보 수정
def update_user_info(user_id, data):
    try:
        conn = dbConnection()
        cursor = conn.cursor()
        
        query = """
        UPDATE user_info
        SET name = %s, user_tel = %s, password = %s
        WHERE id = %s
        """
        cursor.execute(query, (data['name'], data['user_tel'], data['password'], user_id))
        conn.commit()
        return True
    except Exception as e:
        print(f"Error updating user info: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()


# 
def get_edit_user_info_by_id(user_id):
    try:
        conn = dbConnection()  
        cursor = conn.cursor()

        # 사용자 정보 가져오는 쿼리
        query = """
        SELECT id, password, name, user_tel, user_code
        FROM user_info
        WHERE id = %s
        """
        cursor.execute(query, (user_id,))
        result = cursor.fetchone()

        if result:
            return {
                "id": result[0],
                "password": result[1],
                "name": result[2],
                "user_tel": result[3],
                "user_code":result[4]
            }
        else:
            return None
    except psycopg2.Error as db_err:
        print(f"Database error: {db_err}")
        return None
    except Exception as e:
        print(f"Unexpected error: {e}")
        return None
    finally:
        conn.close()



def get_user_custom_info(user_id):
    try:

        print(user_id)
        conn = dbConnection()  
        cursor = conn.cursor()

        # 사용자 정보 가져오는 쿼리
        query = """
        SELECT user_code, area_1, area_1_alias, area_1_address, area_2, area_2_alias, area_2_address
        FROM user_custom_info
        WHERE user_code = %s
        """
        cursor.execute(query, (user_id,))
        result = cursor.fetchone()

        if result:
            return {
                "user_code": result[0],
                "area_1": result[1],
                "area_1_aliase": result[2],
                "area_1_address": result[3],
                "area_2": result[4],
                "area_2_aliase": result[5],
                "area_2_address": result[6]
            }
        else:
            return None
    except psycopg2.Error as db_err:
        print(f"Database error: {db_err}")
        return None
    except Exception as e:
        print(f"Unexpected error: {e}")
        return None
    finally:
        conn.close()

def update_user_selection(user_id, selected_column, valid_columns):
    """
    사용자의 선택 항목을 업데이트하는 함수.
    """
    try:
        conn = dbConnection()
        cursor = conn.cursor()

        # 모든 항목 초기화 쿼리
        reset_query = f"""
        UPDATE user_info
        SET {", ".join([f"{col} = 'N'" for col in valid_columns])}
        WHERE id = %s
        """
        print(f"Reset Query: {reset_query}, Params: {user_id}")  # 디버깅 로그
        cursor.execute(reset_query, (user_id,))

        # 선택된 항목 업데이트 쿼리
        update_query = f"""
        UPDATE user_info
        SET {selected_column} = 'Y'
        WHERE id = %s
        """
        print(f"Update Query: {update_query}, Params: {user_id}")  # 디버깅 로그
        cursor.execute(update_query, (user_id,))

        conn.commit()
        return True
    except Exception as e:
        print(f"DB 업데이트 오류: {e}")  # 디버깅 로그
        conn.rollback()
        return False
    finally:
        conn.close()



def get_user_history(user_id):
    try:
        conn = dbConnection()  
        cursor = conn.cursor()

        # 사용자 정보 가져오는 쿼리
        query  = """
        SELECT user_code, departure, destination, date, destination_address
        FROM user_history
        WHERE user_code = %s
        ORDER BY date desc
        """

        cursor.execute(query, (user_id,))
        result = cursor.fetchall()

        return result
    except psycopg2.Error as db_err:
        print(f"Database error: {db_err}")
        return None
    except Exception as e:
        print(f"Unexpected error: {e}")
        return None
    finally:
        conn.close()

# 즐겨찾기 수정
def update_user_custom_1(user_id, data):
    try:
        conn = dbConnection()
        cursor = conn.cursor()
        
        query = """
        UPDATE user_custom_info
        SET area_1 = %s, area_1_alias = %s, area_1_address = %s
        WHERE user_code = %s
        """
        cursor.execute(query, (data['area'], data['area_alias'], data['area_address'], user_id))
        conn.commit()
        return True
    except Exception as e:
        print(f"Error updating user info: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()

        # 즐겨찾기 수정
def update_user_custom_2(user_id, data):
    try:
        conn = dbConnection()
        cursor = conn.cursor()
        
        query = """
        UPDATE user_custom_info
        SET area_2 = %s, area_2_alias = %s, area_2_address = %s
        WHERE user_code = %s
        """
        cursor.execute(query, (data['area'], data['area_alias'], data['area_address'], user_id))
        conn.commit()
        return True
    except Exception as e:
        print(f"Error updating user info: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()
        

def get_user_ki_history(user_id):
    try:
        conn = dbConnection()  
        cursor = conn.cursor()

        # 사용자 정보 가져오는 쿼리
        query_today  = """
        SELECT user_code, COALESCE(SUM(kilometers), 0) AS total_kilometers
        FROM user_history
        WHERE user_code = %s
        AND DATE(date) = CURRENT_DATE
        GROUP BY user_code
        """

        cursor.execute(query_today, (user_id,))
        result = cursor.fetchone()


        query_yesterday = """
        SELECT user_code, COALESCE(SUM(kilometers), 0) AS total_kilometers
        FROM user_history
        WHERE user_code = %s
        AND DATE(date) = CURRENT_DATE - INTERVAL '1 day'
        GROUP BY user_code
        """
        cursor.execute(query_yesterday, (user_id,))
        result2 = cursor.fetchone()

        ki = result[1] - result2[1] if result[1] - result2[1] >= 0 else 0


        return {
            "ki": ki,
            "today_ki": result[1],
        }
    except psycopg2.Error as db_err:
        print(f"Database error: {db_err}")
        return None
    except Exception as e:
        print(f"Unexpected error: {e}")
        return None
    finally:
        conn.close()


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
        RETURNING user_code
        """
        cursor.execute(query, (id, password, name, user_tel, birthday))

        user_code = cursor.fetchone()[0]

        print(user_code)

        query2 = """
        INSERT INTO user_custom_info (user_code)
        VALUES (%s)
        """
        cursor.execute(query2, (user_code,))

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
    
    query = ("SELECT pklt_nm, now_prk_vhcl_cnt, lat, lot, bsc_prk_crg, add_prk_crg, day_max_crg, ROUND(6371000 * acos(cos(radians(%s)) * cos(radians(lat)) * cos(radians(lot) - radians(%s)) + sin(radians(%s)) * sin(radians(lat)))) AS distance FROM parking_info pi2 ORDER BY distance LIMIT 5")

    cursor.execute(query, (u_lat, u_lot, u_lat))
    result = cursor.fetchall()
    conn.close()

    parking_list = [
        {"name": row[0], "now_prk_vhcl_cnt": row[1], "lat": row[2], "lot": row[3], "bsc_prk_crg": row[4], "add_prk_crg": row[5], "day_max_crg": row[6], "distance": row[7]}
        for row in result
    ]
    return parking_list

def selectPark():
    conn = dbConnection()
    cursor = conn.cursor()

    query = ("SELECT pklt_cd, pklt_nm, addr, tpkct, now_prk_vhcl_cnt, wd_oper_bgng_tm, wd_oper_end_tm, bsc_prk_crg, add_prk_crg, day_max_crg, lat, lot FROM parking_info")

    cursor.execute(query)
    result = cursor.fetchall()
    conn.close()

    parking_list = [
        {"pklt_cd": row[0], "pklt_nm": row[1], "addr": row[2], "tpkct": row[3], "now_prk_vhcl_cnt": row[4], "wd_oper_bgng_tm": row[5], "wd_oper_end_tm": row[6], "bsc_prk_crg": row[7], "add_prk_crg": row[8], "day_max_crg": row[9], "lat": row[10], "lot": row[11]}
        for row in result
    ]
    return parking_list

def insertHistory(name, departure, destination, destination_address, kilometers):
    try:
        conn = dbConnection()
        cursor = conn.cursor()

        # 사용자 ID 조회
        select_query = "SELECT user_code FROM USER_INFO WHERE name = %s"
        cursor.execute(select_query, (name,))
        result = cursor.fetchone()

        if result is None:
            raise Exception("사용자 정보를 찾을 수 없습니다.")

        user_code = result[0]

        # user_history 테이블에 데이터 삽입
        insert_query = """
            INSERT INTO user_history (user_code, departure, destination, kilometers, date, destination_address)
            VALUES (%s, %s, %s, %s, NOW(), %s)
        """
        cursor.execute(insert_query, (user_code, departure, destination, kilometers, destination_address))
        conn.commit()

        return True
    except Exception as e:
        print(f"DB 오류: {e}")
        return False
    finally:
        cursor.close()
        conn.close()