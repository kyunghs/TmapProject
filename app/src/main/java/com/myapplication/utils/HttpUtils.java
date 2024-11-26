package com.myapplication.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final String SERVER_URL = "http://220.116.209.226:8241";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 콜백 인터페이스 정의
    public interface HttpResponseCallback {
        void onSuccess(JSONObject responseData); // 성공 시 JSON 데이터 반환
        void onFailure(String errorMessage);    // 실패 시 에러 메시지 반환
    }

    /**
     * 서버에 JSON 데이터를 POST로 전송하고 응답을 처리
     *
     * @param jsonData   POST 요청에 사용될 데이터
     * @param endpoint   요청 경로
     * @param callback   요청 결과를 처리할 콜백
     */
    public static void sendJsonToServer(JSONObject jsonData, String endpoint, HttpResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = SERVER_URL + endpoint;

        RequestBody body = RequestBody.create(jsonData.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d(TAG, "응답: " + responseData);
                    JSONObject jsonResponse = new JSONObject(responseData);
                    callback.onSuccess(jsonResponse); // 성공 콜백 호출
                } else {
                    Log.e(TAG, "응답 실패 - 코드: " + response.code());
                    callback.onFailure("응답 실패 - 코드: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "네트워크 요청 실패: " + e.getMessage());
                callback.onFailure("네트워크 요청 실패: " + e.getMessage());
            }
        }).start();
    }
}
