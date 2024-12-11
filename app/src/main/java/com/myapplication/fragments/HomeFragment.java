package com.myapplication.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.myapplication.DriveActivity;
import com.myapplication.R;
import com.myapplication.models.Place;
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
    private TextView distance_ye;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Tmapinit();
        // "집으로" 카드 레이아웃 초기화
        LinearLayout homeCardLayout = view.findViewById(R.id.home_card_layout);
        EditText searchHome = view.findViewById(R.id.search_home);
        LinearLayout recent = view.findViewById(R.id.recent);
        // ui
        areaAlias1Text = view.findViewById(R.id.area_alias1_text);
        areaAlias1Address = view.findViewById(R.id.area_alias1_address);
        areaAlias2Text = view.findViewById(R.id.area_alias2_text);
        areaAlias2Address = view.findViewById(R.id.area_alias2_address);
        distance_ce = view.findViewById(R.id.distance_ce);
        distance_ye = view.findViewById(R.id.distance_ye);


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

        areaAlias1Address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopup("37.5548375992165", "126.971732581232");
            }
        });

        recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopup2("37.5734027", "126.9758843");
            }
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
        Log.d("@@@@ 토큰 : ", token);

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

                                // 초기 세팅 값
                                String de_area1 = "즐겨찾기";
                                String de_area1Address = "장소를\n등록 해주세요";
                                String de_area2 = "즐겨찾기";
                                String de_area2Address = "장소를\n등록 해주세요";
                                String de_todayKi = "아직 운행기록이 없어요.";
                                String de_yesterdayKiValue = " ";


                                if (userCustomData != null) {
                                    de_area1 = userCustomData.optString("area_1", "").isEmpty() || userCustomData.isNull("area_1")
                                            ? "즐겨찾기"
                                            : userCustomData.optString("area_1_aliase", "즐겨찾기");

                                    de_area1Address = userCustomData.optString("area_1_address", "").isEmpty() || userCustomData.isNull("area_1_address")
                                            ? "장소를\n등록 해주세요"
                                            : userCustomData.optString("area_1_address");

                                    de_area2 = userCustomData.optString("area_2", "").isEmpty() || userCustomData.isNull("area_2")
                                            ? "즐겨찾기"
                                            : userCustomData.optString("area_2_aliase", "즐겨찾기");

                                    de_area2Address = userCustomData.optString("area_2_address", "").isEmpty() || userCustomData.isNull("area_2_address")
                                            ? "장소를\n등록 해주세요"
                                            : userCustomData.optString("area_2_address");

                                if (get_user_ki_history_data != null) {
                                    de_todayKi = get_user_ki_history_data.optString("today_ki", "db확인필요");
                                    de_yesterdayKiValue = get_user_ki_history_data.optString("ki", "db확인필요");
                                }

                                String displayText = String.format("자동차로\n약 %s km 이동", de_todayKi);
                                String displayText2 = String.format("전일 대비 +%s km 이동", de_yesterdayKiValue);

                                if (de_todayKi == "아직 운행기록이 없어요.") {
                                  distance_ce.setText(de_todayKi);
                                } else {
                                  distance_ce.setText(displayText);
                                }

                                if (de_yesterdayKiValue == " ") {
                                    distance_ye.setText(de_yesterdayKiValue);
                                } else {
                                    distance_ye.setText(displayText2);
                                }


                                // UI 업데이트
                                areaAlias1Text.setText(de_area1);
                                areaAlias1Address.setText(de_area1Address);
                                areaAlias2Text.setText(de_area2);
                                areaAlias2Address.setText(de_area2Address);
//                                distance_ce.setText(displayText);
//                                distance_ye.setText(displayText2);


                                } else {
                                    areaAlias1Text.setText(de_area1);
                                    areaAlias1Address.setText(de_area1Address);
                                    areaAlias2Text.setText(de_area2);
                                    areaAlias2Address.setText(de_area2Address);

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

    private void showPopup(String lat, String lot) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.park_popup);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // "예" 버튼 클릭 이벤트
        dialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("lat", lat);
                jsonData.put("lot", lot);

                // 서버에 JSON 데이터 전송
                HttpUtils.sendJsonToServer(jsonData, "/get/park/info", new HttpUtils.HttpResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseData) {
                        // ParkListBottomSheetFragment 호출 및 데이터 전달
                        ParkListBottomSheetFragment parkListBottomSheet = ParkListBottomSheetFragment.newInstance(responseData.toString());
                        parkListBottomSheet.show(getParentFragmentManager(), "ParkListBottomSheetFragment");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // 에러 처리
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
                dialog.dismiss(); // 다이얼로그 닫기
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "JSON 생성 오류", Toast.LENGTH_SHORT).show();
            }
        });

        // "아니요" 버튼 클릭 이벤트
        dialog.findViewById(R.id.noBtn).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DriveActivity.class);
            intent.putExtra("name", "서울역");
            intent.putExtra("lat", "37.6225571786308 ");
            intent.putExtra("lot", "127.078754902898");
            startActivity(intent);
            dialog.dismiss(); // 현재 다이얼로그 닫기
        });

        dialog.show();
    }

    private void showPopup2(String lat, String lot) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.park_popup);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // "예" 버튼 클릭 이벤트
        dialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("lat", lat);
                jsonData.put("lot", lot);

                // 서버에 JSON 데이터 전송
                HttpUtils.sendJsonToServer(jsonData, "/get/park/info", new HttpUtils.HttpResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseData) {
                        // ParkListBottomSheetFragment 호출 및 데이터 전달
                        ParkListBottomSheetFragment parkListBottomSheet = ParkListBottomSheetFragment.newInstance(responseData.toString());
                        parkListBottomSheet.show(getParentFragmentManager(), "ParkListBottomSheetFragment");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // 에러 처리
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
                dialog.dismiss(); // 다이얼로그 닫기
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "JSON 생성 오류", Toast.LENGTH_SHORT).show();
            }
        });

        // "아니요" 버튼 클릭 이벤트
        dialog.findViewById(R.id.noBtn).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DriveActivity.class);
            intent.putExtra("name", "세종로 공영주차장(시)");
            intent.putExtra("lat", lat);
            intent.putExtra("lot", lot);
            startActivity(intent);
            dialog.dismiss(); // 현재 다이얼로그 닫기
        });

        dialog.show();
    }

}
