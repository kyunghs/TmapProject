package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PwRecoveryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pw_recovery);

        // 전달받은 비밀번호 설정
        String password = getIntent().getStringExtra("password");
        TextView passwordTextView = findViewById(R.id.text_user_pw);
        passwordTextView.setText(password);

        // 로그인 버튼 초기화
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(PwRecoveryActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
