# 검증용 테스트 데이터를 만드는 코드
# author : lmh

import os
import glob
import pandas as pd
import logging

# 로그 설정
logging.basicConfig(level=logging.INFO)

# 테스트 데이터 추출 및 저장
def create_test_data(base_directory, test_data_file, test_ratio=0.2, max_samples=10000):
    logging.info("Creating test data...")
    csv_files = glob.glob(os.path.join(base_directory, "**", "*.csv"), recursive=True)
    logging.info(f"Found {len(csv_files)} CSV files in {base_directory}")

    data_list = []
    for file in csv_files:
        try:
            df = pd.read_csv(file)
            if 'PKLT_CD' not in df or 'NOW_PRK_VHCL_CNT' not in df or 'NOW_PRK_VHCL_UPDT_TM' not in df:
                logging.warning(f"File {file} is missing required columns: {df.columns.tolist()}")
                continue

            df_selected = df[['PKLT_CD', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM']].copy()
            df_selected['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(df_selected['NOW_PRK_VHCL_UPDT_TM'], errors='coerce')
            df_selected.dropna(subset=['NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM'], inplace=True)
            data_list.append(df_selected)
        except Exception as e:
            logging.error(f"Error reading file {file}: {e}")
            continue

    if not data_list:
        raise ValueError("No valid data found for creating test data.")

    df_combined = pd.concat(data_list, ignore_index=True)
    df_combined = df_combined.sort_values('NOW_PRK_VHCL_UPDT_TM')
    
    # 테스트 데이터 분리
    total_samples = min(len(df_combined), max_samples)
    test_data = df_combined.sample(n=total_samples, random_state=42)
    
    # 디렉토리 확인 및 생성
    test_data_dir = os.path.dirname(test_data_file)
    logging.info(f"Test data directory: {test_data_dir}")
    if not os.path.exists(test_data_dir):
        os.makedirs(test_data_dir)
        logging.info(f"Created directory: {test_data_dir}")

    # 데이터 저장
    test_data.to_csv(test_data_file, index=False, encoding='utf-8-sig')
    logging.info(f"Test data saved to {test_data_file}, Total samples: {len(test_data)}")

    return test_data_file


if __name__ == "__main__":
    base_directory = "C:\\Tmap_project_Data\\combined_before\\renamed"  # 날짜 폴더가 있는 최상위 디렉토리
    test_data_file = "C:\\Tmap_project_Data\\testdata\\test_data.csv"  # 테스트 데이터 저장 파일 경로

    # 테스트 데이터 생성
    if not os.path.exists(test_data_file):
        try:
            create_test_data(base_directory, test_data_file, test_ratio=0.2, max_samples=10000)
        except Exception as e:
            logging.error(f"Failed to create test data: {e}")
    else:
        logging.info(f"Test data already exists at {test_data_file}")