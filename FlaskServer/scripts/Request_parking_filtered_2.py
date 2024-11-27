# 실시간 주차장 정보를 요청하여 db에 넣는 코드
# author : lmh

import requests
import pandas as pd
from datetime import datetime
import os
from sqlalchemy import create_engine , text

# API 요청 URL
url = "http://openapi.seoul.go.kr:8088/5a5261555272756439347555534747/json/GetParkingInfo/1001/2000/"
response = requests.get(url)
data = response.json()

# 데이터프레임으로 변환
items = data['GetParkingInfo']['row']
df = pd.DataFrame(items)

# 필요한 컬럼만 선택
columns = ['PKLT_CD', 'PKLT_NM', 'ADDR', 'OPER_SE_NM', 'PRK_STTS_YN', 'TPKCT', 'NOW_PRK_VHCL_CNT','NOW_PRK_VHCL_UPDT_TM', 'NGHT_PAY_YN_NM', 'WD_OPER_BGNG_TM',
           'WD_OPER_END_TM', 'WE_OPER_BGNG_TM', 'WE_OPER_END_TM', 'LHLDY_OPER_BGNG_TM', 'LHLDY_OPER_END_TM', 'SAT_CHGD_FREE_SE', 'LHLDY_CHGD_FREE_SE', 
           'BSC_PRK_CRG', 'BSC_PRK_HR', 'ADD_PRK_CRG', 'ADD_PRK_HR', 'DAY_MAX_CRG', 'LAT', 'LOT']

df_selected = df[columns]


# 필터링 조건 적용
df_filtered = df_selected[(df_selected['OPER_SE_NM'] == '시간제 주차장') & (df_selected['PRK_STTS_YN'] == '1')]
df_filtered = df_filtered.drop_duplicates(subset=['PKLT_CD'])
df_filtered = df_filtered.dropna(subset=['NOW_PRK_VHCL_CNT'])
df_filtered = df_filtered[df_filtered['TPKCT'] > 0]

# CSV 파일 저장 경로 설정
base_directory = "/Tmap_project_Data/desired/"
today_folder = datetime.now().strftime("%Y%m%d")
today_directory = os.path.join(base_directory, today_folder)

if not os.path.exists(today_directory):
    os.makedirs(today_directory)

current_time = datetime.now().strftime("%Y%m%d%H%M")
filename = f"{current_time}_parking_info_filtered_2.csv"
full_path = os.path.join(today_directory, filename)

# CSV로 저장
df_filtered.to_csv(full_path, index=False, encoding='utf-8-sig')

# DB 연결 설정
db_user = 'postgres'
db_password = 'gurtn123'
db_host = 'localhost'
db_name = 'postgres'

try:
    engine = create_engine(f"postgresql+psycopg2://{db_user}:{db_password}@{db_host}/{db_name}")
    table_name = 'parking_info'
    
    # 세션을 열고 삭제 후 삽입
    with engine.connect() as conn:
        df_filtered.columns = [col.lower() for col in df_filtered.columns]  # 컬럼 이름 소문자로 변환
        df_filtered.to_sql(table_name, con=engine, if_exists='append', index=False)
        print("Request_parking_filtered_2 DB insert Success")

except Exception as e:
    print(f"ERROR: {e}")
