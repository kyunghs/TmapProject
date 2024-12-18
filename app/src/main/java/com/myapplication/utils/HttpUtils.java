package com.myapplication.utils;

import android.os.Handler;
import android.os.Looper;
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

    public interface HttpResponseCallback {
        void onSuccess(JSONObject responseData);
        void onFailure(String errorMessage);
    }

    // 인증이 없는 기본 요청
    public static void sendJsonToServer(JSONObject jsonData, String endpoint, HttpResponseCallback callback) {
        sendJsonToServerWithAuth(jsonData, endpoint, null, callback);
    }

    // 인증이 필요한 요청
    public static void sendJsonToServerWithAuth(JSONObject jsonData, String endpoint, String token, HttpResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = SERVER_URL + endpoint;

        if (jsonData == null) {
            jsonData = new JSONObject();
        }

        RequestBody body = RequestBody.create(jsonData.toString(), JSON);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body);

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();

        JSONObject finalJsonData = jsonData;
        new Thread(() -> {
            try {
                Log.d(TAG, "요청 URL: " + url);
                Log.d(TAG, "요청 데이터: " + finalJsonData.toString());
                Log.d(TAG, "요청 헤더: " + request.headers().toString());
                Log.d(TAG, "요청 바디: " + finalJsonData.toString());
                Log.d("HttpUtils", "Authorization 헤더: " + token);
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d(TAG, "응답 성공: " + responseData);

                    JSONObject jsonResponse = new JSONObject(responseData);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(jsonResponse));
                } else {
                    Log.e(TAG, "응답 실패 - 코드: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("응답 실패 - 코드: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "네트워크 요청 실패: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("네트워크 요청 실패: " + e.getMessage()));
            }
        }).start();
    }

    // HttpUtils 클래스에 GET 요청 메서드 추가
    public static void sendGetRequestWithAuth(String endpoint, String token, HttpResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = SERVER_URL + endpoint;

        // 요청 생성
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", token)
                .build();

        Log.d(TAG, "Authorization 헤더: " + token); // 디버깅용 로그 추가

        new Thread(() -> {
            try {
                Log.d(TAG, "요청 URL: " + url);

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d(TAG, "응답 성공: " + responseData);

                    JSONObject jsonResponse = new JSONObject(responseData);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(jsonResponse));
                } else {
                    Log.e(TAG, "응답 실패 - 코드: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("응답 실패 - 코드: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "네트워크 요청 실패: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("네트워크 요청 실패: " + e.getMessage()));
            }
        }).start();
    }

}
