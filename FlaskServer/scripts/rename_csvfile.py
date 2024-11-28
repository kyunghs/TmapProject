# 특정 기점으로 *(7월) 서울시에서 제공하는 데이터의 컬럼명이 달라, 해당 디렉토리 내 컬렴명을 통일화 시키는 코드
# author : lmh

import os
import pandas as pd
import glob

# CSV 파일이 있는 최상위 디렉토리
input_directory = "/Tmap_project_Data/combined_before"  # 최상위 디렉토리 경로
output_directory = os.path.join(input_directory, "renamed")  # 변경된 파일 저장 디렉토리

# 저장 디렉토리가 없으면 생성
if not os.path.exists(output_directory):
    os.makedirs(output_directory)

# 변경할 칼럼 이름 매핑
column_mapping = {
    "PARKING_CODE": "PKLT_CD",
    "CUR_PARKING": "NOW_PRK_VHCL_CNT",
    "CUR_PARKING_TIME": "NOW_PRK_VHCL_UPDT_TM"
}

# 최상위 디렉토리 내의 날짜 폴더 탐색
date_folders = [os.path.join(input_directory, folder) for folder in os.listdir(input_directory) if os.path.isdir(os.path.join(input_directory, folder))]

# 날짜 폴더별 처리
for folder in date_folders:
    # 날짜 폴더 내 모든 CSV 파일 검색
    csv_files = glob.glob(os.path.join(folder, "*.csv"))
    
    for file in csv_files:
        try:
            # CSV 파일 읽기
            df = pd.read_csv(file)
            
            # 칼럼 이름 변경
            df.rename(columns=column_mapping, inplace=True)
            
            # 출력 경로 설정 (날짜 폴더 구조 유지)
            relative_folder = os.path.relpath(folder, input_directory)
            output_subdirectory = os.path.join(output_directory, relative_folder)
            
            if not os.path.exists(output_subdirectory):
                os.makedirs(output_subdirectory)
            
            # 변경된 파일 저장
            output_file = os.path.join(output_subdirectory, os.path.basename(file))
            df.to_csv(output_file, index=False, encoding="utf-8-sig")
            
            print(f"Processed and saved: {output_file}")
        except Exception as e:
            print(f"Error processing file {file}: {e}")

print("All files processed successfully!")