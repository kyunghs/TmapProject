package com.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserEditActivity extends AppCompatActivity {

    private EditText nameField, phoneField, passwordField;
    private String currentName, currentPhone, currentPassword; // 현재 사용자 정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit);

        // UI 초기화
        nameField = findViewById(R.id.name_field);
        phoneField = findViewById(R.id.phone_field);
        passwordField = findViewById(R.id.password_field);

        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        // 사용자 데이터 가져오기
        fetchUserData();

        // 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener(v -> saveUserData());

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener(v -> finish());
    }

    private void fetchUserData() {
        // SharedPreferences에서 토큰 가져오기
        String token = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserEditActivity", "토큰이 없습니다.");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        token = "Bearer " + token; // Authorization 헤더 준비

        // GET 요청으로 데이터 가져오기
        HttpUtils.sendGetRequestWithAuth("/getEditUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");
                            currentName = userData.optString("name", "");
                            currentPhone = userData.optString("user_tel", "");
                            currentPassword = ""; // 보안 상 비밀번호는 비워둠

                            // 필드 업데이트
                            nameField.setText(currentName);
                            phoneField.setText(currentPhone);
                            passwordField.setText(currentPassword);
                        } else {
                            String message = responseData.optString("message", "데이터를 불러올 수 없습니다.");
                            Toast.makeText(UserEditActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("UserEditActivity", "JSON 처리 오류", e);
                        Toast.makeText(UserEditActivity.this, "데이터 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("UserEditActivity", "서버 요청 실패: " + errorMessage);
                    Toast.makeText(UserEditActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveUserData() {
        // SharedPreferences에서 토큰 가져오기
        String token = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserEditActivity", "토큰이 없습니다.");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return; // 토큰이 없으면 저장 요청을 보내지 않음
        }

        token = "Bearer " + token; // "Bearer" 붙이기

        String updatedName = nameField.getText().toString().trim();
        String updatedPhone = phoneField.getText().toString().trim();
        String updatedPassword = passwordField.getText().toString().trim();

        // 입력값이 없으면 기존 값을 유지
        String finalName = updatedName.isEmpty() ? currentName : updatedName;
        String finalPhone = updatedPhone.isEmpty() ? currentPhone : updatedPhone;
        String finalPassword = updatedPassword.isEmpty() ? currentPassword : updatedPassword;

        JSONObject updateData = new JSONObject();
        try {
            updateData.put("name", finalName);
            updateData.put("user_tel", finalPhone);
            updateData.put("password", finalPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터 생성 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UserEditActivity", "토큰: " + token);

        // 서버 요청
        HttpUtils.sendJsonToServerWithAuth(updateData, "/updateUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                Log.d("UserEditActivity", "응답 성공: " + responseData.toString());
                runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            Toast.makeText(UserEditActivity.this, "업데이트 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UserEditActivity.this, "업데이트 실패", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserEditActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("UserEditActivity", "서버 요청 실패: " + errorMessage);
                    Toast.makeText(UserEditActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}
