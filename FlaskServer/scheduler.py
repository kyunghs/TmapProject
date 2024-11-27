import os
import logging
from apscheduler.schedulers.background import BackgroundScheduler
from concurrent.futures import ThreadPoolExecutor
import subprocess

# 로그 설정
logging.basicConfig(
    filename='scheduler.log',
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# 스크립트 실행 함수
def run_script(script_name):
    try:
        script_path = os.path.abspath(os.path.join("scripts", script_name))
        if not os.path.isfile(script_path):
            raise FileNotFoundError(f"Script {script_name} not found at {script_path}")
        result = subprocess.run(["python", script_path], capture_output=True, text=True)
        logging.info(f"Running script: {script_name}")
        logging.info(f"Output: {result.stdout}")
        if result.stderr:
            logging.error(f"Error: {result.stderr}")
    except Exception as e:
        logging.error(f"Exception while running script {script_name}: {e}")

# 스케줄러 초기화
scheduler = BackgroundScheduler()

# TIME.SLEEP 사용 시 충돌 문제 발생하여 비동기로 처리
executor = ThreadPoolExecutor()

# 작업 스케줄링
scheduler.add_job(
    func=lambda: run_script("Delete_parking_info.py"),
    trigger="cron",
    minute="*/5",
    id="delete_parking_info",
    misfire_grace_time=300  # 5분 안에 누락된 작업 실행 허용
)

scheduler.add_job(
    func=lambda: executor.submit(run_script, "Request_parking_filtered_1.py"),
    trigger="cron",
    minute="*/5",
    id="request_parking_1"
)

scheduler.add_job(
    func=lambda: executor.submit(run_script, "Request_parking_filtered_2.py"),
    trigger="cron",
    minute="*/5",
    id="request_parking_2"
)

scheduler.add_job(
    func=lambda: run_script("all_filterd.py"),
    trigger="cron",
    hour=0,
    minute=0,
    id="all_filtered"
)

# 스케줄러 실행
if __name__ == "__main__":
    try:
        logging.info("Starting Scheduler...")
        scheduler.start()
        logging.info("Scheduler started successfully.")
        while True:
            pass  # Keep the scheduler running
    except (KeyboardInterrupt, SystemExit):
        logging.info("Shutting down Scheduler...")
        scheduler.shutdown()
