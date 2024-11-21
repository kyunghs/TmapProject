package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // "집으로" 카드 레이아웃 초기화
        LinearLayout homeCardLayout = view.findViewById(R.id.home_card_layout);
        EditText search_edit_text = view.findViewById(R.id.search_edit_text);
        ImageView search_icon = view.findViewById(R.id.search_icon);

        // 클릭 이벤트 설정
        homeCardLayout.setClickable(true);
        homeCardLayout.setFocusable(true);
        homeCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // BookmarkBottomSheetFragment 호출
                BookmarkBottomSheetFragment bottomSheet = new BookmarkBottomSheetFragment();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                bottomSheet.show(fragmentManager, bottomSheet.getTag());
            }
        });

        search_edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Enter 키 동작 처리
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // EditText의 값을 가져와 URL에 포함
                    String inputText = search_edit_text.getText().toString();
                    OkHttpClient client = new OkHttpClient();

                    try {
                        String encodedKeyword = URLEncoder.encode(inputText, "UTF-8"); // 검색어를 URL에 맞게 인코딩

                        // 동적으로 URL 생성
                        String url = "https://apis.openapi.sk.com/tmap/pois?version=1&searchKeyword=" + encodedKeyword +
                                "&searchType=all&searchtypCd=A&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&page=1&count=5&multiPoint=N&poiGroupYn=N";

                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .addHeader("Accept", "application/json")
                                .addHeader("appKey", "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp")
                                .build();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Response response = client.newCall(request).execute();
                                    String responseBody = response.body().string();

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // JSON 파싱
                                                JSONObject rootObject = new JSONObject(responseBody);
                                                JSONObject searchPoiInfo = rootObject.getJSONObject("searchPoiInfo");
                                                JSONObject pois = searchPoiInfo.getJSONObject("pois");
                                                JSONArray poiArray = pois.getJSONArray("poi");

                                                // 첫 번째 POI의 위도와 경도 추출
                                                JSONObject firstPoi = poiArray.getJSONObject(0);
                                                String latitude = firstPoi.getString("frontLat");
                                                String longitude = firstPoi.getString("frontLon");

                                                // HTTP POST 요청으로 위도와 경도 전송
                                                OkHttpClient client = new OkHttpClient();
                                                String url = "http://220.116.209.226:8241/get/park/info";

                                                JSONObject jsonBody = new JSONObject();
                                                jsonBody.put("lat", latitude);
                                                jsonBody.put("lot", longitude);

                                                RequestBody body = RequestBody.create(
                                                        jsonBody.toString(),
                                                        MediaType.get("application/json; charset=utf-8")
                                                );

                                                Request request = new Request.Builder()
                                                        .url(url)
                                                        .post(body)
                                                        .build();

                                                // POST 요청 실행
                                                client.newCall(request).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                        Log.e("POST Request", "POST 요청 실패: " + e.getMessage());
                                                    }

                                                    @Override
                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                        if (response.isSuccessful()) {
                                                            String responseData = response.body().string();
                                                            Log.e("POST Response", "응답: " + responseData);
                                                        } else {
                                                            Log.e("POST Response", "POST 요청 실패 - 응답 코드: " + response.code());
                                                        }
                                                    }
                                                });

                                                // Toast 메시지로 출력
                                                Toast.makeText(getActivity(), "위도: " + latitude + ", 경도: " + longitude, Toast.LENGTH_LONG).show();


                                                // Toast 메시지로 출력
                                                Toast.makeText(getActivity(), "위도: " + latitude + ", 경도: " + longitude, Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(requireContext(), DriveActivity.class);
                                                intent.putExtra("lat", latitude);
                                                intent.putExtra("lot", longitude);
                                                startActivity(intent);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getActivity(), "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true; // 이벤트 처리 완료
                }
                return false; // 기본 동작 유지
            }
        });


        return view;
    }
}
