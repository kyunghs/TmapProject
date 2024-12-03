package com.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
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
        nameText = view.findViewById(R.id.name_text); // 이름 텍스트
        //phoneText = view.findViewById(R.id.phone_text); // 전화번호 텍스트
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
        HttpUtils.sendJsonToServer(null, "/getUserInfo", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");
                            String name = userData.getString("name");
                            //String phone = userData.getString("phone");

                            // UI 업데이트
                            nameText.setText(name);
                            //phoneText.setText(phone);
                        } else {
                            Toast.makeText(getActivity(), "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "데이터 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "오류: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
