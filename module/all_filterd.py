import pandas as pd
import os
import glob
from datetime import datetime

# 오늘 날짜 폴더 생성
today_folder = datetime.now().strftime("%Y%m%d")  # 예: 20241109
base_directory = os.path.join(rf"C:\grad\Reqest_parking_filterd\n_desired", today_folder)  # 상대 경로: n_desired/20241109
print(base_directory)
combined_directory = os.path.join("combined", today_folder)  # 상대 경로: combined/20241109

# 통합 파일 저장을 위한 디렉토리 생성
if not os.path.exists(combined_directory):
    os.makedirs(combined_directory)

# 현재 시간 포맷으로 파일 이름에 사용할 시간 생성
current_time = datetime.now().strftime("%Y%m%d%H%M")

# 기본 디렉토리의 모든 CSV 파일 검색
csv_files = glob.glob(os.path.join(base_directory, "*.csv"))

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
        columns = ['PKLT_CD', 'PKLT_NM', 'ADDR', 'OPER_SE_NM', 'PRK_STTS_YN', 'TPKCT', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM', 
                   'NGHT_PAY_YN_NM', 'WD_OPER_BGNG_TM', 'WD_OPER_END_TM', 'WE_OPER_BGNG_TM', 'WE_OPER_END_TM', 
                   'LHLDY_OPER_BGNG_TM', 'LHLDY_OPER_END_TM', 'SAT_CHGD_FREE_SE', 'LHLDY_CHGD_FREE_SE', 
                   'BSC_PRK_CRG', 'BSC_PRK_HR', 'ADD_PRK_CRG', 'ADD_PRK_HR', 'DAY_MAX_CRG', 'LAT', 'LOT']
        
        df_selected = df[columns]  # 필요한 열만 선택하여 새로운 데이터프레임 생성

        # 운영 방식이 시간제 주차장인 경우만 필터링
        df_filtered = df_selected[df_selected['OPER_SE_NM'] == '시간제 주차장']
        
        # 현재 주차 차량 수가 결측값이 아닌 경우만 필터링
        df_filtered = df_filtered[df_filtered['NOW_PRK_VHCL_CNT'].notna()]
            
        # 필터링된 데이터프레임을 리스트에 추가
        df_list.append(df_filtered)
        
        # 필터링된 데이터를 현재 날짜 폴더에 저장
        filename = f"{current_time}_filtered_{os.path.basename(file)}"
        full_path = os.path.join(base_directory, filename)
        df_filtered.to_csv(full_path, index=False, encoding='utf-8-sig')
        
        print("@@@")
        print(f"Success filtered data saved to {full_path}")
        print("\n")

    # 데이터가 존재할 때만 통합 진행
    if df_list:
        # 모든 필터링된 데이터프레임을 하나의 데이터프레임으로 통합
        df_combined = pd.concat(df_list, ignore_index=True)

        # 통합 파일을 저장할 경로 및 파일명 설정 (오늘 날짜 사용)
        combined_filename = f"{today_folder}_combined_filtered_parking_info.csv"
        combined_full_path = os.path.join(combined_directory, combined_filename)
        df_combined.to_csv(combined_full_path, index=False, encoding='utf-8-sig')

        print("@@@")
        print(f"Combined data saved to {combined_full_path}")
        print("\n")

        # 기존 개별 CSV 파일 삭제
        for file in csv_files:
            os.remove(file)
            print("@@@")
            print(f"Deleted original file: {file}")
            print("\n")
    else:
        print("No data available to combine.")
