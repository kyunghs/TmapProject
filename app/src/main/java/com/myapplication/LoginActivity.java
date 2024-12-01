package com.myapplication;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // UI 요소 초기화
        EditText editTextId = findViewById(R.id.editTextId);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.test);
        TextView findIdPw = findViewById(R.id.find_id_pw);
        TextView registerText = findViewById(R.id.register_text); // 회원가입 텍스트 추가

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(v -> {
            // 사용자 입력값 가져오기
            String id = editTextId.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // 입력값 검증
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // JSON 데이터 생성
            JSONObject loginData = new JSONObject();
            try {
                loginData.put("id", id);
                loginData.put("password", password);
            } catch (JSONException e) {
                Log.e(TAG, "JSON 생성 오류: " + e.getMessage());
                Toast.makeText(LoginActivity.this, "로그인 요청 생성 중 오류 발생", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버 요청
            HttpUtils.sendJsonToServer(loginData, "/Login", new HttpUtils.HttpResponseCallback() {

            @Override
                public void onSuccess(JSONObject responseData) {
                    try {
                        boolean loginSuccess = responseData.getBoolean("success");
                        if (loginSuccess) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = responseData.optString("message", "로그인 실패");
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON 파싱 오류: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "로그인 응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "로그인 실패: " + errorMessage);
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "서버 요청 중 오류 발생: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });

        });

        // "아이디 · 비밀번호 찾기" 버튼 클릭 이벤트
        findIdPw.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, IdPwChActivity.class);
            startActivity(intent); // id_pw_change.xml 화면으로 이동
        });

        // "회원가입 하기" 버튼 클릭 이벤트 추가
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent); // 회원가입 화면으로 이동
        });
    }
}
