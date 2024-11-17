package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FindIDPWActivity extends AppCompatActivity {
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

        // 기본 상태: 아이디 찾기
        setToFindId();

        // 탭 클릭 이벤트 처리
        tabFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFindId();
            }
        });

        tabFindPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFindPw();
            }
        });

        // 버튼 클릭 이벤트 처리
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionButton.getText().toString().equals("아이디 찾기")) {
                    handleFindId();
                } else if (actionButton.getText().toString().equals("비밀번호 찾기")) {
                    handleFindPw();
                }
            }
        });
    }

    // 아이디 찾기 상태로 설정
    private void setToFindId() {
        tabFindId.setTextColor(getResources().getColor(android.R.color.black));
        tabFindPw.setTextColor(getResources().getColor(android.R.color.darker_gray));
        inputEmail.setVisibility(View.GONE);
        actionButton.setText("아이디 찾기");
    }

    // 비밀번호 찾기 상태로 설정
    private void setToFindPw() {
        tabFindId.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabFindPw.setTextColor(getResources().getColor(android.R.color.black));
        inputEmail.setVisibility(View.VISIBLE);
        actionButton.setText("비밀번호 찾기");
    }

    // 아이디 찾기 로직
    private void handleFindId() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Intent intent = new Intent(FindIDPWActivity.this, IdNotFoundActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(FindIDPWActivity.this, IdRecoveryActivity.class);
            startActivity(intent);
        }
    }

    // 비밀번호 찾기 로직
    private void handleFindPw() {
        String email = inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            // 입력 값이 없을 경우 password_not_found.xml로 이동
            Intent intent = new Intent(FindIDPWActivity.this, PwNotFoundActivity.class);
            startActivity(intent);
        } else {
            // 입력 값이 있을 경우 password_recovery.xml로 이동
            Intent intent = new Intent(FindIDPWActivity.this, PwRecoveryActivity.class);
            startActivity(intent);
        }
    }
}
