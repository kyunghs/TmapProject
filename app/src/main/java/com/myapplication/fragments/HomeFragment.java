package com.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.myapplication.R;
import com.myapplication.utils.HttpSearchUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.utils.HttpUtils;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;

import org.json.JSONException;
import org.json.JSONObject;
public class HomeFragment extends Fragment {
    private final static String CLIENT_ID = "";
    private final static String API_KEY = "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp";
    private final static String USER_KEY = "";
    private final static String DEVICE_KEY = "";

    private TextView areaAlias1Text;
    private TextView areaAlias1Address;
    private TextView areaAlias2Text;
    private TextView areaAlias2Address;
    private TextView distance_ce;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Tmapinit();
        // "집으로" 카드 레이아웃 초기화
        LinearLayout homeCardLayout = view.findViewById(R.id.home_card_layout);
        EditText searchHome = view.findViewById(R.id.search_home);

        // ui
        areaAlias1Text = view.findViewById(R.id.area_alias1_text);
        areaAlias1Address = view.findViewById(R.id.area_alias1_address);
        areaAlias2Text = view.findViewById(R.id.area_alias2_text);
        areaAlias2Address = view.findViewById(R.id.area_alias2_address);
        distance_ce = view.findViewById(R.id.distance_ce);


        fetchMainInfo();
        // 클릭 이벤트 설정
        homeCardLayout.setClickable(true);
        homeCardLayout.setFocusable(true);
        homeCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // BookmarkBottomSheetFragment 호출 및 스타일 지정
                BookmarkBottomSheetFragment bottomSheet = new BookmarkBottomSheetFragment();
                bottomSheet.setStyle(
                        BottomSheetDialogFragment.STYLE_NORMAL,
                        R.style.AppBottomSheetDialogBorder20WhiteTheme
                );
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                bottomSheet.show(fragmentManager, bottomSheet.getTag());
            }
        });

        //목적지 검색 시
        searchHome.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String inputText = searchHome.getText().toString();
                HttpSearchUtils.performSearch(inputText, requireContext(), getParentFragmentManager());
                return true;
            }
            return false;
        });


        return view;
    }

    public void Tmapinit(){
        TmapUISDK.Companion.initialize(getActivity(), CLIENT_ID, API_KEY, USER_KEY, DEVICE_KEY, new TmapUISDK.InitializeListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFail(int i, @Nullable String s) {
            }

            @Override
            public void savedRouteInfoExists(@Nullable String dest) {
            }
        });
    }

    private void fetchMainInfo() {
        String token =  getActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 요청으로 사용자 정보 가져오기
        HttpUtils.sendJsonToServerWithAuth(null,"/main/dashboard", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        Log.d("UserFragment", "서버 응답2: " + responseData.toString());

                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.optJSONObject("data");
                            if (userData != null) {
                                JSONObject userCustomData = userData.optJSONObject("user_custom_data");
                                JSONObject get_user_ki_history_data = userData.optJSONObject("get_user_ki_history_data");
                                if (userCustomData != null) {

                                String area1 = userCustomData.optString("area_1", "정보 없음");
                                String area1Address = userCustomData.optString("area_1_address", "주소 없음");
                                String area2 = userCustomData.optString("area_2", "정보 없음");
                                String area2Address = userCustomData.optString("area_2_address", "주소 없음");
//                                String distanceKilo = get_user_ki_history_data.optString("distanceKilo", "잘못불러옴");

                                // UI 업데이트
                                areaAlias1Text.setText(area1);
                                areaAlias1Address.setText(area1Address);
                                areaAlias2Text.setText(area2);
                                areaAlias2Address.setText(area2Address);
//                                distance_ce.setText(distanceKilo);




                                }


//                                Log.d("UserFragment", "UI 업데이트 완료 - 이름: " + name);
                            } else {
                                Toast.makeText(getActivity(), "유효한 사용자 데이터를 받을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = responseData.optString("message", "오류 발생");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "데이터 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserFragment", "JSONException", e);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("UserFragment", "요청 실패: " + errorMessage);
                });
            }
        });
    }

}
