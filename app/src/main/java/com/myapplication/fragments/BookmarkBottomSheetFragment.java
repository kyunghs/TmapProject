package com.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.myapplication.utils.HttpUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;
import com.myapplication.utils.HttpUtils;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;

import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkBottomSheetFragment extends BottomSheetDialogFragment {

    private final static String API_KEY = "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp";
    private final static String CLIENT_ID = "";
    private final static String USER_KEY = "";
    private final static String DEVICE_KEY = "";
    private TextView area1_alias;
    private TextView area1_name;
    private TextView area1_address;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // bookmark_bottom_sheet.xml 레이아웃 연결
        View view = inflater.inflate(R.layout.bookmark_bottom_sheet, container, false);
        Tmapinit();

        area1_alias = view.findViewById(R.id.editTextNickname);
        area1_name = view.findViewById(R.id.ediTextName);
        area1_address = view.findViewById(R.id.editAddress);

        // 키보드의 확인 버튼 이벤트 처리
        area1_alias.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 확인 버튼 눌렀을 때 동작
                Toast.makeText(getActivity(), "확인 버튼 눌림: " + area1_alias.getText().toString(), Toast.LENGTH_SHORT).show();

                // 필요한 추가 로직 (예: 서버로 데이터 전송 등)

                return true; // 이벤트 처리 완료
            }
            return false;
        });

        fetchMainInfo();

        // ImageView 클릭 이벤트 처리
        ImageView editIcon = view.findViewById(R.id.editIcon);
        editIcon.setOnClickListener(v -> {
            // BookmarkEditBottomSheetFragment 표시
            FragmentManager fragmentManager = getParentFragmentManager();
            BookmarkEditBottomSheetFragment editBottomSheet = new BookmarkEditBottomSheetFragment();
            editBottomSheet.setStyle(
                    BottomSheetDialogFragment.STYLE_NORMAL,
                    R.style.AppBottomSheetDialogBorder20WhiteTheme
            );
            editBottomSheet.show(fragmentManager, editBottomSheet.getTag());
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

                                // 초기 세팅 값
                                String de_area1_alias = "별명을 설정 해주세요.";
                                String de_area1_name = "목적지 이름";
                                String de_area1Address = "목적지 주소";

                                if (userCustomData != null) {
                                    de_area1_alias = userCustomData.optString("area_1_aliase", "즐겨찾기").isEmpty()
                                            ? "즐겨찾기"
                                            : userCustomData.optString("area_1_aliase");

                                    de_area1_name = userCustomData.optString("area_1", "즐겨찾기").isEmpty()
                                            ? "즐겨찾기"
                                            : userCustomData.optString("area_1");

                                    de_area1Address = userCustomData.optString("area_1_address", "장소를\n등록 해주세요").isEmpty()
                                            ? "장소를\n등록 해주세요"
                                            : userCustomData.optString("area_1_address");


                                    // UI 업데이트
//                                    areaAlias1Text.setText(de_area1);
                                    area1_alias.setText(de_area1_alias);
                                    area1_name.setText(de_area1_name);
                                    area1_address.setText((de_area1Address));


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
