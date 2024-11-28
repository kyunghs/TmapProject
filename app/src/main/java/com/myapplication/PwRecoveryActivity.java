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

        if (password != null && !password.isEmpty()) {
            passwordTextView.setText(password);
        } else {
            passwordTextView.setText("비밀번호를 불러올 수 없습니다.");
        }

        passwordTextView.setText(password);

        // 로그인 버튼 이벤트 처리
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(PwRecoveryActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
