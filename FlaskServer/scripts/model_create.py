# 자동화 이전에 데이터를 가지고 별도로 모델을 만드는 코드
# author : lmh

import os
import glob
import logging
import pickle
import pandas as pd
from datetime import datetime
from statsmodels.tsa.arima.model import ARIMA

# 로그 설정
logging.basicConfig(level=logging.INFO)

# ARIMA 모델 학습 함수
def train_model(csv_files, model_directory, start_date, end_date):
    try:
        logging.info(f"Training model for files: {csv_files}")
        data_list = []

        # CSV 파일 데이터 읽기
        for file in csv_files:
            try:
                df = pd.read_csv(file)
                if 'PKLT_CD' not in df or 'NOW_PRK_VHCL_CNT' not in df or 'NOW_PRK_VHCL_UPDT_TM' not in df:
                    logging.warning(f"File {file} is missing required columns.")
                    continue

                df_selected = df[['PKLT_CD', 'NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM']].copy()
                df_selected['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(df_selected['NOW_PRK_VHCL_UPDT_TM'], errors='coerce')
                df_selected.dropna(subset=['NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM'], inplace=True)
                data_list.append(df_selected)
            except Exception as e:
                logging.error(f"Error reading file {file}: {e}")
                continue

        if not data_list:
            raise ValueError("No valid data found for training.")

        # 데이터 결합 및 정렬
        df_combined = pd.concat(data_list, ignore_index=True)
        df_combined = df_combined.sort_values('NOW_PRK_VHCL_UPDT_TM')
        df_combined.set_index('NOW_PRK_VHCL_UPDT_TM', inplace=True)

        os.makedirs(model_directory, exist_ok=True)

        models = {}
        for pklt_cd, group in df_combined.groupby('PKLT_CD'):
            try:
                model = ARIMA(group['NOW_PRK_VHCL_CNT'], order=(5, 1, 0))
                model_fit = model.fit()
                models[pklt_cd] = model_fit
                logging.info(f"Trained ARIMA model for parking lot {pklt_cd}")
            except Exception as e:
                logging.error(f"Error training ARIMA model for {pklt_cd}: {e}")
                continue

        if not models:
            raise ValueError("No models were trained.")

        # 모델 파일 저장
        model_filepath = os.path.join(
            model_directory, f'arima_parking_model_{start_date}_to_{end_date}.pkl'
        )
        with open(model_filepath, 'wb') as file:
            pickle.dump(models, file)

        logging.info(f"Model training completed. Models saved at: {model_filepath}")
        return model_filepath
    except Exception as e:
        logging.error(f"Training failed: {e}")
        raise


if __name__ == "__main__":
    base_directory = "C:\\Tmap_project_Data\\combined_before\\renamed"  # 날짜 폴더가 있는 최상위 디렉토리
    model_directory = "C:\\Tmap_project_Data\\model_dev"      # 모델 파일 저장 디렉토리

    # 날짜별 폴더 탐색 및 정렬
    date_folders = sorted(
        [os.path.join(base_directory, folder) for folder in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, folder))]
    )

    file_increment = 10
    current_files = []

    for i, folder in enumerate(date_folders):
        current_files.extend(glob.glob(os.path.join(folder, "*.csv")))

        if len(current_files) >= file_increment:
            start_date = os.path.basename(date_folders[0])
            end_date = os.path.basename(folder)

            train_model(current_files[:file_increment], model_directory, start_date, end_date)
            file_increment += 10