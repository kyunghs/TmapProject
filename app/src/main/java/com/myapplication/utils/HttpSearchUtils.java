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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpSearchUtils {

    private static final String API_KEY = "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp";

    public static void performSearch(String keyword, Context context, FragmentManager fragmentManager) {
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

                    // UI 스레드에서 실행
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

    private static void showPlacesBottomSheet(String poiData, FragmentManager fragmentManager) {
        PlacesBottomSheetFragment placesBottomSheet = PlacesBottomSheetFragment.newInstance(poiData);
        placesBottomSheet.show(fragmentManager, "PlacesBottomSheetFragment");
    }
}
