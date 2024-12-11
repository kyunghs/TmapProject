package com.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class ParkingTTSSTTActivity extends AppCompatActivity {

    private static final String TAG = "ParkingTTSSTTActivity";
    private static final long LISTENING_TIMEOUT = 5000; // 5초 타임아웃
    private TTSSTTHelper ttssttHelper;
    private TextView recognizedTextView;
    private Handler timeoutHandler;
    private boolean isListening; // 음성 인식 중인지 확인하는 플래그
    private boolean hasReceivedResult; // 결과를 받은 여부를 체크

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tts_top_sheet);

        // Helper 초기화
        ttssttHelper = new TTSSTTHelper(this);

        // Recognized TextView 초기화
        recognizedTextView = findViewById(R.id.recognized_text);

        // Voice Icon 클릭 이벤트
        ImageView voiceIcon = findViewById(R.id.voice_icon);
        voiceIcon.setOnClickListener(v -> {
            String message = "잔여석이 있는 부근 주차장을 안내할까요?";
            ttssttHelper.speakText(message);

            // TextView 초기화
            recognizedTextView.setText("");
            recognizedTextView.setVisibility(View.VISIBLE);

            // 음성 인식 시작
            startListeningWithTimeout();
        });

        // 팝업 닫기 버튼
        ImageView closeIcon = findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(v -> finish());
    }

    private void startListeningWithTimeout() {
        if (isListening) {
            return; // 이미 음성 인식 중이면 실행하지 않음
        }

        hasReceivedResult = false; // 결과 초기화
        isListening = true; // 음성 인식 상태로 설정

        timeoutHandler = new Handler(Looper.getMainLooper());

        // 음성 인식 시작
        ttssttHelper.startListening(new SpeechRecognitionListener());

        // 5초 타임아웃 설정
        timeoutHandler.postDelayed(() -> {
            if (isListening && !hasReceivedResult) {
                // 5초가 지나도 결과를 받지 못한 경우
                ttssttHelper.speakText("음성을 다시 말씀해주세요.");
                isListening = false; // 음성 인식 종료로 설정
            }
        }, LISTENING_TIMEOUT);
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
            // 사용자가 말하기 시작했을 때
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 음성 입력 소리 크기 변화
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 음성 입력 버퍼 처리
        }

        @Override
        public void onEndOfSpeech() {
            // 사용자가 말하기를 멈췄을 때
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "음성 인식 오류 발생: " + error);
            isListening = false; // 음성 인식 종료로 설정
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> resultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (resultList != null && !resultList.isEmpty()) {
                String recognizedText = resultList.get(0); // 첫 번째 결과 가져오기
                recognizedTextView.setVisibility(View.VISIBLE); // TextView 보이도록 설정
                recognizedTextView.setText(recognizedText); // 결과 텍스트 설정
                recognizedTextView.invalidate(); // 화면 갱신
                Log.d("TTSSTT", "Recognized Text: " + recognizedText); // 디버깅 로그

                // 사용자가 말한 텍스트가 화면에 표시되었으니 화면 닫기
                new Handler().postDelayed(ParkingTTSSTTActivity.this::finish, 2000);

            } else {
                Log.d("TTSSTT", "No recognized text found");
                ttssttHelper.speakText("인식된 음성이 없습니다. 다시 시도해주세요.");
            }
        }





        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> partialResultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (partialResultsList != null && !partialResultsList.isEmpty()) {
                String partialText = partialResultsList.get(0); // 첫 번째 중간 결과 가져오기
                Log.d("TTSSTT", "Partial Recognized Text: " + partialText); // 중간 결과 로그 출력
                updateRecognizedText(partialText); // 화면에 업데이트
            }
        }


        @Override
        public void onEvent(int eventType, Bundle params) {
            // 이벤트 처리
        }
    }

    private void updateRecognizedText(String text) {
        Log.d("TTSSTT", "Updating TextView with: " + text); // 디버깅 로그
        runOnUiThread(() -> {
            recognizedTextView.setVisibility(View.VISIBLE); // 보이도록 설정
            recognizedTextView.setText(text); // 텍스트 업데이트
            recognizedTextView.invalidate(); // 화면 갱신
        });
    }





    private void handleUserResponse(String response) {
        if (response.contains("네") || response.contains("예") || response.contains("응")) {
            ttssttHelper.speakText("경로를 재탐색하겠습니다.");
            // TODO: 경로 재탐색 로직 추가
        } else {
            ttssttHelper.speakText("기존 목적지로 안내하겠습니다.");
            // TODO: 기존 목적지 안내 로직 추가
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
        ttssttHelper.shutdown();
        super.onDestroy();
    }
}