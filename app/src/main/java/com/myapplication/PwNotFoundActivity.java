package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PwNotFoundActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_not_found);

        // 실패 메시지 설정
        String errorMessage = getIntent().getStringExtra("errorMessage");


        // 로그인 버튼 초기화
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(PwNotFoundActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
