package com.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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

        Button saveButton = view.findViewById(R.id.bookmark_save);
        saveButton.setOnClickListener(v -> onSaveButtonClick());

        // 키보드의 확인 버튼 이벤트 처리
        area1_alias.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 확인 버튼 눌렀을 때 동작
                return true; // 이벤트 처리 완료
            }
            return false;
        });

        fetchMainInfo();

        // ImageView 클릭 이벤트 처리
        ImageView editIcon = view.findViewById(R.id.editIcon);
        editIcon.setOnClickListener(v -> {
            String updatedName = "세종문화회관";
            String updatedAddress = "서울 종로구 세종대로 175";

            // 변경된 값으로 UI 업데이트
            area1_name.setText(updatedName);
            area1_address.setText(updatedAddress);

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

    private void onSaveButtonClick() {
        String alias = area1_alias.getText().toString();
        String name = area1_name.getText().toString();
        String address = area1_address.getText().toString();

        if (alias.isEmpty() || name.isEmpty() || address.isEmpty()) {
            Toast.makeText(getActivity(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("area_alias", alias);
            jsonData.put("area", name);
            jsonData.put("area_address", address);
            jsonData.put("category", 1);
            Log.d("@@ : ", "JSON 데이터 생성 성공: " + jsonData.toString());
        } catch (JSONException e) {
            Log.e("@@ : ", "JSON 데이터 생성 중 오류 발생", e);
        }

        String token = requireActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.sendJsonToServerWithAuth(jsonData, "/update_user_custom_data", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                requireActivity().runOnUiThread(() -> {
                    Log.d("@@ : responseData", responseData.toString());
                    Toast.makeText(getActivity(), "저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    // fetchMainInfo 호출로 UI 갱신
//                    fetchMainInfo();

                    // dismiss() 호출로 현재 화면 종료
                    dismiss();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "저장 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

    public void Tmapinit() {
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
        String token = getActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 요청으로 사용자 정보 가져오기
        HttpUtils.sendJsonToServerWithAuth(null, "/main/dashboard", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        Log.d("UserFragment", "서버 응답2: " + responseData.toString());

                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.optJSONObject("data");
                            if (userData != null) {
                                JSONObject userCustomData = userData.optJSONObject("user_custom_data");
                                Log.d("@@@ : ", String.valueOf(userCustomData));

                                // 초기 세팅 값
                                String de_area1_alias = "별명을 설정 해주세요.";
                                String de_area1_name = "목적지 이름";
                                String de_area1Address = "목적지 주소";

                                if (userCustomData != null) {
                                    // area_1_aliase 처리
                                    if (userCustomData.isNull("area_1_aliase") || userCustomData.optString("area_1_aliase").isEmpty()) {
                                        de_area1_alias = "별명을 설정 해주세요.";
                                    } else {
                                        de_area1_alias = userCustomData.optString("area_1_aliase");
                                    }

                                    // area_1 처리
                                    if (userCustomData.isNull("area_1") || userCustomData.optString("area_1").isEmpty()) {
                                        de_area1_name = "목적지 이름";
                                    } else {
                                        de_area1_name = userCustomData.optString("area_1");
                                    }

                                    // area_1_address 처리
                                    if (userCustomData.isNull("area_1_address") || userCustomData.optString("area_1_address").isEmpty()) {
                                        de_area1Address = "목적지 주소";
                                    } else {
                                        de_area1Address = userCustomData.optString("area_1_address");
                                    }
                                }

                                // UI 업데이트
                                area1_alias.setText(de_area1_alias);
                                area1_name.setText(de_area1_name);
                                area1_address.setText(de_area1Address);



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
