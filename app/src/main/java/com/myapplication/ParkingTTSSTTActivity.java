package com.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
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
                Toast.makeText(this, "음성을 다시 말씀해주세요.", Toast.LENGTH_SHORT).show();
                isListening = false; // 음성 인식 종료로 설정
            }
        }, LISTENING_TIMEOUT);
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(ParkingTTSSTTActivity.this, "음성을 말씀해주세요.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ParkingTTSSTTActivity.this, "음성 인식 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
            isListening = false; // 음성 인식 종료로 설정
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> resultList = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            if (resultList != null && !resultList.isEmpty()) {
                String recognizedText = resultList.get(0).toLowerCase(Locale.ROOT);
                hasReceivedResult = true; // 결과를 받은 것으로 설정
                isListening = false; // 음성 인식 종료로 설정
                timeoutHandler.removeCallbacksAndMessages(null); // 타이머 해제
                updateRecognizedText(recognizedText);
                handleUserResponse(recognizedText);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 음성 인식 중간 결과 처리
            ArrayList<String> partialResultsList = partialResults.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            if (partialResultsList != null && !partialResultsList.isEmpty()) {
                String partialText = partialResultsList.get(0);
                updateRecognizedText(partialText);
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 이벤트 처리
        }
    }

    private void updateRecognizedText(String text) {
        runOnUiThread(() -> {
            recognizedTextView.setVisibility(View.VISIBLE);
            recognizedTextView.setText(text);
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