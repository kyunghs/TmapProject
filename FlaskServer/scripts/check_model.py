# 만들어진 모델에 parking code key값이 있는지 확인
# author : lmh

import os
import pickle
import logging

# 로그 설정
logging.basicConfig(level=logging.INFO)

def check_parking_code_in_model(model_file, parking_code):
    try:
        # 모델 파일 로드
        logging.info(f"Loading model from: {model_file}")
        with open(model_file, 'rb') as file:
            models = pickle.load(file)

        # 모델에 포함된 parking_code 목록
        parking_codes = list(models.keys())
        logging.info(f"Parking codes in the model: {parking_codes}")

        # parking_code 확인
        if parking_code in parking_codes:
            logging.info(f"Parking code '{parking_code}' is found in the model.")
            return True
        else:
            logging.warning(f"Parking code '{parking_code}' is NOT found in the model.")
            return False
    except FileNotFoundError:
        logging.error(f"Model file not found: {model_file}")
        return False
    except Exception as e:
        logging.error(f"An error occurred: {e}")
        return False


if __name__ == "__main__":
    # 모델 파일 경로
    model_directory = "C:\\Tmap_project_Data\\models"  # 모델 파일이 저장된 디렉토리
    model_file_name = "arima_parking_model_20240415_to_20240504.pkl"  # 모델 파일 이름
    model_file = os.path.join(model_directory, model_file_name)

    # 확인할 parking_code
    parking_code = "171721"  # 예: 확인하려는 주차장 코드

    # parking_code 확인
    exists = check_parking_code_in_model(model_file, parking_code)
    if exists:
        print(f"Parking code '{parking_code}' is included in the model.")
    else:
        print(f"Parking code '{parking_code}' is NOT included in the model.")