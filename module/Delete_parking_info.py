# 5분마다 수집한 실시간 주차장 정보 테이블을 비우는 소스
# author : lmh

import psycopg2

# 데이터베이스 연결 정보
db_user = 'postgres'
db_password = 'gurtn123'
db_host = '220.116.209.226'
db_name = 'postgres'

try:
    # 데이터베이스에 연결
    conn = psycopg2.connect(
        dbname=db_name,
        user=db_user,
        password=db_password,
        host=db_host
    )
    cur = conn.cursor()
    
    # 데이터 삭제 실행
    cur.execute("DELETE FROM parking_info;")
    conn.commit()  # 변경 사항 커밋
    print("All data deleted from parking_info table.")
    
    # 커서와 연결 닫기
    cur.close()
    conn.close()
except Exception as e:
    print(f"ERROR: {e}")
