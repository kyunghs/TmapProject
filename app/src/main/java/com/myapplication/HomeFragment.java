package com.myapplication;

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

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
                                            Log.e("Response", responseBody);
                                            Toast.makeText(getActivity(), "Response: " + responseBody, Toast.LENGTH_SHORT).show();
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
