package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // 회원가입 텍스트 초기화
        TextView registerText = findViewById(R.id.register_text);
        TextView findIdPw = findViewById(R.id.find_id_pw);

        // 회원가입 클릭 이벤트 추가
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // RegisterActivity로 이동
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 아이디 · 비밀번호 찾기 클릭 이벤트 추가
        findIdPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FindIDPWActivity로 이동
                Intent intent = new Intent(LoginActivity.this, FindIDPWActivity.class);
                startActivity(intent);
            }
        });
    }
}
