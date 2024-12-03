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
                .post(body)
                .addHeader("Content-Type", "application/json");

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();

        JSONObject finalJsonData = jsonData;
        new Thread(() -> {
            try {
                Log.d(TAG, "요청 URL: " + url);
                Log.d(TAG, "요청 데이터: " + finalJsonData.toString());

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
