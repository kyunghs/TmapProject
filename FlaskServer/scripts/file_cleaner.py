# 수집한 데이터를 날짜별로 구성하는 코드
# author : lmh

import os
import shutil
from datetime import datetime

# 기준 디렉토리 설정
base_directory = "C:\grad\수집데이터_1 (2)\수집데이터_1"  # 원본 파일들이 있는 디렉토리
destination_directory = "/Tmap_project_Data/sorted"  # 파일을 옮길 디렉토리

# 디렉토리 생성 함수
def ensure_directory_exists(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)

# 파일 이름에서 날짜 추출 함수
def extract_date_from_filename(filename):
    try:
        # 예: 202404181918_parking_info_1.csv
        date_part = filename.split("_")[0]  # 첫 번째 "_" 이전 부분 추출
        datetime.strptime(date_part, "%Y%m%d%H%M")  # 유효성 검사
        return date_part[:8]  # 날짜(YYYYMMDD) 부분만 반환
    except (IndexError, ValueError):
        return None

# 기본 디렉토리 내 모든 CSV 파일 찾기
csv_files = [f for f in os.listdir(base_directory) if f.endswith(".csv")]

# 파일 이동 작업
for file in csv_files:
    file_path = os.path.join(base_directory, file)
    if os.path.isfile(file_path):
        # 파일 이름에서 날짜 추출
        date = extract_date_from_filename(file)
        if date:
            # 날짜 디렉토리 생성
            date_directory = os.path.join(destination_directory, date)
            ensure_directory_exists(date_directory)
            
            # 파일 이동
            destination_path = os.path.join(date_directory, file)
            shutil.move(file_path, destination_path)
            print(f"Moved: {file} -> {destination_path}")
        else:
            print(f"Skipping: {file} (invalid format)")
    else:
        print(f"Skipping: {file} (not a file)")

print("File sorting completed!")