package com.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserEditActivity extends AppCompatActivity {

    private EditText nameField, phoneField, passwordField;
    private String currentName, currentPhone, currentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit);

        // UI 요소 초기화
        nameField = findViewById(R.id.name_field);
        phoneField = findViewById(R.id.phone_field);
        passwordField = findViewById(R.id.password_field);
        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        // 사용자 정보 가져오기
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
        String token = "Bearer " + getSharedPreferences("AuthPrefs", MODE_PRIVATE).getString("auth_token", "");

        HttpUtils.sendGetRequestWithAuth("/getUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");
                            currentName = userData.getString("name");
                            currentPhone = userData.getString("user_tel"); // 서버 필드명 확인 필요
                            currentPassword = ""; // 비밀번호는 보안상 미전송

                            // 필드에 기존 값 설정
                            nameField.setText(currentName);
                            phoneField.setText(currentPhone);
                            passwordField.setText(""); // 보안상 비밀번호는 공백 유지
                        } else {
                            Toast.makeText(UserEditActivity.this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(UserEditActivity.this, "데이터 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(UserEditActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                    finish();
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
            updateData.put("user_tel", finalPhone); // 서버 필드명 확인 필요
            updateData.put("password", finalPassword);
        } catch (JSONException e) {
            Toast.makeText(this, "데이터 생성 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버 요청
        HttpUtils.sendJsonToServer(updateData, "/updateUserInfo", new HttpUtils.HttpResponseCallback() {
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
                        Toast.makeText(UserEditActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
