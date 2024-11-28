# 만들어진 모델들을 검증하는 코드
# author : lmh

import os
import pickle
import pandas as pd
import logging
from sklearn.metrics import mean_squared_error

# 로그 설정
logging.basicConfig(level=logging.INFO)

# 모델 검증 함수
def validate_model(model_file, test_data_file):
    try:
        # 모델 로드
        with open(model_file, 'rb') as file:
            models = pickle.load(file)

        logging.info(f"Loaded model from {model_file}")

        # 테스트 데이터 로드
        test_data = pd.read_csv(test_data_file)
        test_data['NOW_PRK_VHCL_UPDT_TM'] = pd.to_datetime(test_data['NOW_PRK_VHCL_UPDT_TM'], errors='coerce')
        test_data.dropna(subset=['NOW_PRK_VHCL_CNT', 'NOW_PRK_VHCL_UPDT_TM'], inplace=True)

        results = {}
        for pklt_cd, group in test_data.groupby('PKLT_CD'):
            if pklt_cd not in models:
                logging.warning(f"No model found for parking lot {pklt_cd}")
                continue

            model = models[pklt_cd]
            y_true = group['NOW_PRK_VHCL_CNT']
            y_pred = model.forecast(len(y_true))

            mse = mean_squared_error(y_true, y_pred)
            results[pklt_cd] = mse

        logging.info("Validation completed.")
        return results
    except Exception as e:
        logging.error(f"Validation failed: {e}")
        raise


if __name__ == "__main__":
    model_file = "/path/to/models/arima_parking_model_20240415_to_20240430.pkl"
    test_data_file = "/path/to/test_data.csv"

    validation_results = validate_model(model_file, test_data_file)
    print("Validation Results:", validation_results)