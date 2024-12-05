import os
import logging
from apscheduler.schedulers.background import BackgroundScheduler
from concurrent.futures import ThreadPoolExecutor
import subprocess
import time
import signal

# 로그 설정
logging.basicConfig(
    filename='scheduler.log',
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

console_handler = logging.StreamHandler()
console_handler.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
console_handler.setFormatter(formatter)
logging.getLogger().addHandler(console_handler)

# 스크립트 실행 함수
def run_script(script_name):
    try:
        script_path = os.path.abspath(os.path.join("scripts", script_name))
        if not os.path.isfile(script_path):
            raise FileNotFoundError(f"Script {script_name} not found at {script_path}")
        
        logging.info(f"@@@@ : {script_path}")
        result = subprocess.run(["python", script_path], capture_output=True, text=True)
        
        logging.info(f"Running script: {script_name}")
        logging.info(f"Output: {result.stdout}")
        if result.stderr:
            logging.error(f"Error: {result.stderr}")
    except Exception as e:
        logging.error(f"Exception while running script {script_name}: {e}")

# 스케줄러 초기화
scheduler = BackgroundScheduler()
executor = ThreadPoolExecutor()

# 주기적으로 실행할 작업 추가
scheduler.add_job(
    func=lambda: run_script("Delete_parking_info.py"),
    trigger="cron",
    minute="*/5",
    id="delete_parking_info",
    misfire_grace_time=300
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

# 자정마다 실행할 작업 추가 (누락된 작업)
scheduler.add_job(
    func=lambda: run_script("all_filterd.py"),
    trigger="cron",
    hour=0,
    minute=0,
    id="all_filtered"
)

# 종료 신호 처리 함수
def handle_exit(signum, frame):
    logging.info("Received exit signal. Shutting down...")
    if scheduler.running:
        scheduler.shutdown(wait=False)
    executor.shutdown(wait=False)
    logging.info("Scheduler and Executor have been shut down.")
    exit(0)

signal.signal(signal.SIGINT, handle_exit)
signal.signal(signal.SIGTERM, handle_exit)

# 메인 실행부
if __name__ == "__main__":
    try:
        logging.info("Starting Scheduler...")
        scheduler.start()
        logging.info("Scheduler started successfully.")
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        handle_exit(None, None)
    except Exception as e:
        logging.error(f"Unexpected error: {e}")
