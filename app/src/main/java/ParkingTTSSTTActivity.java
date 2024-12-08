import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.R;

import java.util.ArrayList;
import java.util.Locale;

public class ParkingTTSSTTActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STT = 2000;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tts_top_sheet);

        // TTS 초기화
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.KOREAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "언어가 지원되지 않습니다.");
                }
            } else {
                Log.e("TTS", "TTS 초기화 실패");
            }
        });

        // TTS 실행 (Voice Icon 클릭)
        ImageView voiceIcon = findViewById(R.id.voice_icon);
        voiceIcon.setOnClickListener(v -> {
            String message = "잔여석이 있는 부근 주차장을 안내할까요?";
            speakText(message);
            listenForUserResponse();
        });

        // 팝업 닫기 버튼
        ImageView closeIcon = findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(v -> finish());
    }

    // TTS 실행 함수
    private void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        }
    }

    // 사용자 음성 응답 대기
    private void listenForUserResponse() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "부근 주차장 안내를 원하시면 '네'라고 말해주세요.");
        startActivityForResult(intent, REQUEST_CODE_STT);
    }

    // STT 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_STT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String recognizedText = results.get(0).toLowerCase(Locale.ROOT);
                handleUserResponse(recognizedText);
            }
        }
    }

    // 사용자 응답 처리
    private void handleUserResponse(String response) {
        if (response.contains("네") || response.contains("예") || response.contains("응")) {
            speakText("경로를 재탐색하겠습니다.");
            // TODO: 경로 재탐색 로직 추가 (잔여석이 있는 가장 가까운 주차장 검색)
            Toast.makeText(this, "경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();
        } else {
            speakText("기존 목적지로 안내하겠습니다.");
            // TODO: 기존 목적지 안내 로직 추가
            Toast.makeText(this, "기존 목적지로 안내합니다.", Toast.LENGTH_SHORT).show();
            finish(); // 팝업 닫기
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
