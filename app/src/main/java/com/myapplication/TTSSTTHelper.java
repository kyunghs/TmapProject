package com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTSSTTHelper {
    private static final String TAG = "TTSSTTHelper";
    private final Activity activity;
    private TextToSpeech tts;
    private final SpeechRecognizer speechRecognizer;

    public TTSSTTHelper(Activity activity) {
        this.activity = activity;

        // TTS 초기화
        tts = new TextToSpeech(activity, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.KOREAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS 언어 지원되지 않음");
                    tts = null;
                }
            } else {
                Log.e(TAG, "TTS 초기화 실패");
                tts = null;
            }
        });

        // STT 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
    }

    public void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        } else {
            Log.e(TAG, "TTS가 초기화되지 않았습니다.");
        }
    }

    public void startListening(RecognitionListener listener) {
        Intent intent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(intent);
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}