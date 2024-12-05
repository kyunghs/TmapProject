package com.myapplication.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.myapplication.fragments.PlacesBottomSheetFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpSearchUtils {

    private static final String API_KEY = "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp";
    private static final String ROUTE_URL = "https://apis.openapi.sk.com/tmap/routes?version=1&callback=function";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 콜백 인터페이스 정의
    public interface RouteRequestCallback {
        void onSuccess(JSONObject rootObject);
        void onError(String errorMessage);
    }

    public static void performSearch(String keyword, Context context, FragmentManager fragmentManager) {
        // 기존 검색 메서드는 그대로 유지
        OkHttpClient client = new OkHttpClient();

        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = "https://apis.openapi.sk.com/tmap/pois?version=1&searchKeyword=" + encodedKeyword +
                    "&searchType=all&searchtypCd=A&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&page=1&count=5&multiPoint=N&poiGroupYn=N";

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("appKey", API_KEY)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            try {
                                JSONObject rootObject = new JSONObject(responseBody);
                                JSONObject searchPoiInfo = rootObject.getJSONObject("searchPoiInfo");
                                JSONObject pois = searchPoiInfo.getJSONObject("pois");
                                JSONArray poiArray = pois.getJSONArray("poi");

                                // 검색 결과를 BottomSheet에 표시
                                showPlacesBottomSheet(poiArray.toString(), fragmentManager);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void performRouteRequest(Context context, int way, double startX, double startY, double endX, double endY, RouteRequestCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // 요청 데이터 생성
        String jsonBody = "{"
                + "\"tollgateFareOption\": 16,"
                + "\"roadType\": 32,"
                + "\"directionOption\": 1,"
                + "\"endX\": " + endX + ","
                + "\"endY\": " + endY + ","
                + "\"endRpFlag\": \"G\","
                + "\"reqCoordType\": \"WGS84GEO\","
                + "\"startX\": " + startX + ","
                + "\"startY\": " + startY + ","
                + "\"gpsTime\": \"20191125153000\","
                + "\"searchOption\": " + way + ","
                + "\"speed\": 10,"
                + "\"uncetaintyP\": 1,"
                + "\"uncetaintyA\": 1,"
                + "\"uncetaintyAP\": 1,"
                + "\"carType\": 0,"
                + "\"startName\": \"%EC%9D%84%EC%A7%80%EB%A1%9C%20%EC%9E%85%EA%B5%AC%EC%97%AD\","
                + "\"endName\": \"%ED%97%A4%EC%9D%B4%EB%A6%AC\","
                + "\"detailPosFlag\": \"2\","
                + "\"resCoordType\": \"WGS84GEO\","
                + "\"sort\": \"index\""
                + "}";

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(ROUTE_URL)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("appKey", API_KEY)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        JSONObject rootObject = new JSONObject(responseBody);
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                // 성공 콜백 호출
                                callback.onSuccess(rootObject);
                            });
                        }
                    } catch (JSONException e) {
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                callback.onError("JSON 파싱 오류: " + e.getMessage());
                            });
                        }
                    }
                } else {
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            callback.onError("HTTP 오류: " + response.message());
                        });
                    }
                }
            } catch (IOException e) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        callback.onError("네트워크 오류: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private static void showPlacesBottomSheet(String poiData, FragmentManager fragmentManager) {
        PlacesBottomSheetFragment placesBottomSheet = PlacesBottomSheetFragment.newInstance(poiData);
        placesBottomSheet.show(fragmentManager, "PlacesBottomSheetFragment");
    }
}