package com.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class IdPwChActivity extends AppCompatActivity {


    private static final String TAG = "IdPwChActivity";

    private TextView tabFindId, tabFindPw;
    private EditText inputName, inputPhone, inputEmail;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_pw_change);

        // UI 요소 초기화
        tabFindId = findViewById(R.id.tab_find_id);
        tabFindPw = findViewById(R.id.tab_find_pw);
        inputName = findViewById(R.id.input_name);
        inputPhone = findViewById(R.id.input_phone);
        inputEmail = findViewById(R.id.input_email);
        actionButton = findViewById(R.id.action_button);

        // 기본은 "아이디 찾기" 상태
        setTabState(true);

        // 탭 클릭 이벤트
        tabFindId.setOnClickListener(v -> setTabState(true));
        tabFindPw.setOnClickListener(v -> setTabState(false));

        // 버튼 클릭 이벤트
        actionButton.setOnClickListener(v -> {
            if (actionButton.getText().toString().equals("아이디 찾기")) {
                findUserId();
            } else {
                findPassword();
            }
        });
    }

    private void setTabState(boolean isFindId) {
        if (isFindId) {
            // 아이디 찾기 상태
            tabFindId.setTextColor(Color.BLACK);
            tabFindPw.setTextColor(Color.GRAY);
            inputEmail.setVisibility(View.GONE);
            actionButton.setText("아이디 찾기");
        } else {
            // 비밀번호 찾기 상태
            tabFindId.setTextColor(Color.GRAY);
            tabFindPw.setTextColor(Color.BLACK);
            inputEmail.setVisibility(View.VISIBLE);
            actionButton.setText("비밀번호 찾기");
        }
    }

    private void findUserId() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "이름과 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("name", name);
            requestData.put("phone", phone);
        } catch (JSONException e) {
            Log.e(TAG, "JSON 생성 오류: " + e.getMessage());
            Toast.makeText(this, "요청 데이터 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.sendJsonToServer(requestData, "/findUserId", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        boolean success = responseData.getBoolean("success");
                        if (success) {
                            String userId = responseData.getString("userId");
                            navigateToIdRecoveryScreen(userId);
                        } else {
                            String message = responseData.optString("message", "아이디를 찾을 수 없습니다.");
                            Toast.makeText(IdPwChActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "응답 파싱 오류: " + e.getMessage());
                        Toast.makeText(IdPwChActivity.this, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(IdPwChActivity.this, "요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void findPassword() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("name", name);          // 이름
            requestData.put("id", email);           // 서버가 요구하는 ID 필드
            requestData.put("user_tel", phone);
        } catch (JSONException e) {
            Log.e(TAG, "JSON 생성 오류: " + e.getMessage());
            Toast.makeText(this, "요청 데이터 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.sendJsonToServer(requestData, "/findUserPw", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        boolean success = responseData.getBoolean("success");
                        if (success) {
                            String password = responseData.getString("password");
                            navigateToPwRecoveryScreen(password);
                        } else {
                            String message = responseData.optString("message", "비밀번호를 찾을 수 없습니다.");
                            navigateToPwNotFoundScreen(message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "응답 파싱 오류: " + e.getMessage());
                        Toast.makeText(IdPwChActivity.this, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(IdPwChActivity.this, "요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void navigateToPwRecoveryScreen(String password) {
        Intent intent = new Intent(this, PwRecoveryActivity.class);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private void navigateToPwNotFoundScreen(String message) {
        Intent intent = new Intent(this, PwNotFoundActivity.class);
        intent.putExtra("errorMessage", message);
        startActivity(intent);
    }

    private void navigateToIdRecoveryScreen(String userId) {
        Intent intent = new Intent(this, IdRecoveryActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}
