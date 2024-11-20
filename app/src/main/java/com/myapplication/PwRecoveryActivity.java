package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PwRecoveryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_recovery);

        // 로그인 버튼 초기화
        Button loginButton = findViewById(R.id.login_button);

        // 버튼 클릭 이벤트 처리
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PwRecoveryActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
