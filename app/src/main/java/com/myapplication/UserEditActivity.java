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
    private String currentName, currentPhone, currentPassword; // 기존 사용자 정보
    private String token; // 토큰을 클래스 멤버 변수로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit);

        // UI 요소 초기화
        nameField = findViewById(R.id.name_field); // 이름 입력 필드
        phoneField = findViewById(R.id.phone_field); // 전화번호 입력 필드
        passwordField = findViewById(R.id.password_field); // 비밀번호 입력 필드
        Button saveButton = findViewById(R.id.save_button); // 저장 버튼
        Button cancelButton = findViewById(R.id.cancel_button); // 취소 버튼

        // SharedPreferences에서 토큰 가져오기
        token = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserEditActivity", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return; // 토큰이 없으면 화면을 종료합니다.
        }

        token = "Bearer " + token; // "Bearer" 붙이기

        // 서버에서 기존 사용자 정보를 불러옵니다.
        fetchUserData();

        // 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener(v -> saveUserData());

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener(v -> {
            Toast.makeText(this, "편집이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료
        });
    }

    // 사용자 데이터를 서버에서 가져오는 함수
    private void fetchUserData() {
        HttpUtils.sendJsonToServerWithAuth(null, "/getEditUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");
                            currentName = userData.optString("name", ""); // 이름
                            currentPhone = userData.optString("user_tel", ""); // 전화번호
                            currentPassword = ""; // 보안상 비밀번호는 공백으로 유지

                            // 필드 업데이트
                            nameField.setText(currentName);
                            phoneField.setText(currentPhone);
                        } else {
                            String message = responseData.optString("message", "사용자 정보를 불러오지 못했습니다.");
                            Toast.makeText(UserEditActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(UserEditActivity.this, "데이터 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserEditActivity", "JSONException", e);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(UserEditActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("UserEditActivity", "요청 실패: " + errorMessage);
                });
            }
        });
    }

    // 사용자 데이터를 저장하는 함수
    private void saveUserData() {
        String updatedName = nameField.getText().toString().trim();
        String updatedPhone = phoneField.getText().toString().trim();
        String updatedPassword = passwordField.getText().toString().trim();

        // 입력값이 없으면 기존 값을 유지
        String finalName = updatedName.isEmpty() ? currentName : updatedName;
        String finalPhone = updatedPhone.isEmpty() ? currentPhone : updatedPhone;
        String finalPassword = updatedPassword.isEmpty() ? currentPassword : updatedPassword;

        // JSON 데이터 생성
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("name", finalName);
            updateData.put("user_tel", finalPhone); // 필드 이름 변경
            updateData.put("password", finalPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터 생성 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버 요청
        HttpUtils.sendJsonToServerWithAuth(updateData, "/updateUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        boolean success = responseData.getBoolean("success");
                        String message = responseData.getString("message");
                        Toast.makeText(UserEditActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            finish(); // 성공적으로 업데이트되면 액티비티 종료
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserEditActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
