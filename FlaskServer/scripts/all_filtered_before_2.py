# 특정기점으로 *(7월) 서울시에서 제공하는 데이터의 컬럼명이 달라 기존 소스와 다르게 컬렴명이 다름
# author : lmh

import pandas as pd
import os
import glob
from datetime import datetime

# 오늘 날짜 폴더 생성
today_folder = datetime.now().strftime("%Y%m%d")  # 예: 20241109
day_folder = "20240704"
base_directory = os.path.join("C:\grad\수집데이터_1 (2)\수집데이터_1", day_folder)
print(base_directory)
combined_directory = os.path.join("/Tmap_project_Data/combined_before", day_folder)  # Windows 경로: C:\Tmap_project_Data\combined\20241109

# 통합 파일 저장을 위한 디렉토리 생성
if not os.path.exists(combined_directory):
    os.makedirs(combined_directory)

# 현재 시간 포맷으로 파일 이름에 사용할 시간 생성
current_time = datetime.now().strftime("%Y%m%d%H%M")

# 기본 디렉토리의 모든 CSV 파일 검색
csv_files = glob.glob(os.path.join(base_directory, "*.csv"))
# print("%%%%%%%%%%%%",csv_files)

# CSV 파일이 존재하는지 확인
if not csv_files:
    print("No CSV files found in the directory.")
else:
    # 데이터프레임을 저장할 리스트 초기화
    df_list = []

    # 각 CSV 파일을 처리하는 반복문
    for file in csv_files:
        df = pd.read_csv(file)  # CSV 파일 읽기
        
        # 필요한 컬럼만 선택
        required_columns  = [
            'PARKING_CODE', 'PARKING_NAME', 'ADDR', 'PARKING_TYPE_NM', 'OPERATION_RULE_NM', 
            'CAPACITY', 'CUR_PARKING', 'CUR_PARKING_TIME', 'PAY_YN', 'RATES', 
            'ADD_RATES', 'DAY_MAXIMUM', 'LAT', 'LNG'
        ]

                # 데이터프레임에서 존재하는 칼럼만 선택
        available_columns = [col for col in required_columns if col in df.columns]
        df_selected = df[available_columns]
        

        # 운영 방식이 시간제 주차장인 경우만 필터링
        df_filtered = df_selected[df_selected['OPERATION_RULE_NM'] == '시간제 주차장']
        
        # 현재 주차 차량 수가 결측값이 아닌 경우만 필터링
        df_filtered = df_filtered[df_filtered['CUR_PARKING'].notna()]
            
        # 필터링된 데이터프레임을 리스트에 추가
        df_list.append(df_filtered)
        
        # 필터링된 데이터를 현재 날짜 폴더에 저장
        filename = f"{current_time}_filtered_{os.path.basename(file)}"
        full_path = os.path.join(base_directory, filename)
        df_filtered.to_csv(full_path, index=False, encoding='utf-8-sig')
        
        # print("@@@")
        print(f"Success filtered data saved to {full_path}")
        # print("\n")

    # 데이터가 존재할 때만 통합 진행
    if df_list:
        # 모든 필터링된 데이터프레임을 하나의 데이터프레임으로 통합
        df_combined = pd.concat(df_list, ignore_index=True)

        # 통합 파일을 저장할 경로 및 파일명 설정 (오늘 날짜 사용)
        combined_filename = f"{day_folder}_combined_filtered_parking_info.csv"
        combined_full_path = os.path.join(combined_directory, combined_filename)
        df_combined.to_csv(combined_full_path, index=False, encoding='utf-8-sig')

        # print("@@@")
        print(f"Combined data saved to {combined_full_path}")
        # print("\n")

        # 기존 개별 CSV 파일 삭제
        deleted_files = []

        for file in csv_files:
            os.remove(file)
            deleted_files.append(file)
            # print("@@@")
            print(f"Deleted original file: {file}")
            # print("\n")
        
        # 삭제된 파일 목록 기록
        deleted_log_file = os.path.join(combined_directory, "deleted_files_log.txt")
        with open(deleted_log_file, "w", encoding="utf-8") as log_file:
            log_file.write("Deleted files:\n")
            log_file.write("\n".join(deleted_files))

        # print("@@@")
        print(f"Deleted files log saved to {deleted_log_file}")
        # print("\n")
    else:
        print("No data available to combine.")