package com.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IdPwChActivity extends AppCompatActivity {

    private TextView tabFindId, tabFindPw;
    private EditText inputEmail;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_pw_change);

        tabFindId = findViewById(R.id.tab_find_id);
        tabFindPw = findViewById(R.id.tab_find_pw);
        inputEmail = findViewById(R.id.input_email);
        actionButton = findViewById(R.id.action_button);

        // 기본은 아이디 찾기 상태
        setTabState(true);

        tabFindId.setOnClickListener(v -> setTabState(true));
        tabFindPw.setOnClickListener(v -> setTabState(false));
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
}
