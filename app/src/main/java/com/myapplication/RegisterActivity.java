package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPasswordConfirm, inputName, inputPhone, inputBirthday;
    private Button registBtn, passStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // UI 요소 초기화
        inputEmail = findViewById(R.id.editTextEmail);
        inputPassword = findViewById(R.id.editTextPassword);
        inputPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        inputName = findViewById(R.id.editTextName);
        inputPhone = findViewById(R.id.editTextPhone);
        inputBirthday = findViewById(R.id.editTextBirthday);
        registBtn = findViewById(R.id.registBtn);
        passStartBtn = findViewById(R.id.passStartBtn);

        // "내비서 시작하기" 버튼 클릭 이벤트
        registBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // "프리패스 시작" 버튼 클릭 이벤트
        passStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity로 이동
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleRegister() {
        // 입력값 가져오기
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String passwordConfirm = inputPasswordConfirm.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String birthday = inputBirthday.getText().toString().trim();

        // 필수 입력값 검증
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm) ||
                TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(birthday)) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버 요청 데이터 생성
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("id", email);
            requestData.put("password", password);
            requestData.put("name", name);
            requestData.put("user_tel", phone);
            requestData.put("birthday", birthday);
        } catch (JSONException e) {
            Toast.makeText(this, "회원가입 요청 데이터 생성 중 오류 발생", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버에 회원가입 요청
        HttpUtils.sendJsonToServer(requestData, "/register", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Toast.makeText(RegisterActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            // LoginActivity로 이동
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(RegisterActivity.this, "응답 처리 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

}
