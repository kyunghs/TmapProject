package com.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapplication.R;
import com.myapplication.UserEditActivity;
import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserFragment extends Fragment {

    private TextView nameText, phoneText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // UI 요소 초기화
        nameText = view.findViewById(R.id.name_text);
        //phoneText = view.findViewById(R.id.phone_text);
        Button editProfileButton = view.findViewById(R.id.edit_profile_button);

        // 유저 정보 로드
        fetchUserInfo();

        // "프로필 편집" 버튼 클릭 이벤트
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserEditActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchUserInfo() {
        // SharedPreferences에서 토큰 가져오기
        String token = getActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token == null || token.isEmpty()) {
            Log.e("UserFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        token = "Bearer " + token;
        Log.d("UserFragment", "토큰: " + token);

        HttpUtils.sendJsonToServerWithAuth(null, "/getUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        Log.d("UserFragment", "서버 응답: " + responseData.toString());

                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.optJSONObject("data");
                            if (userData != null) {
                                String name = userData.optString("name", "알 수 없음");
                                //String phone = userData.optString("user_tel", "알 수 없음");

                                // UI 업데이트
                                nameText.setText(name);
                                //phoneText.setText(phone);

                                Log.d("UserFragment", "UI 업데이트 완료 - 이름: " + name);
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
