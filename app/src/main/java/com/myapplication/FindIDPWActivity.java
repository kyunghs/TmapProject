package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

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
            Toast.makeText(this, "이름과 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 아이디 찾기 로직을 구현하거나 API를 호출하세요.
        Toast.makeText(this, "아이디 찾기 기능 구현 필요", Toast.LENGTH_SHORT).show();
    }

    // 비밀번호 찾기 로직
    private void handleFindPw() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 요청 데이터 생성
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("name", name);
            requestData.put("id", email);       // 서버에서 "id"를 기대
            requestData.put("user_tel", phone); // 서버에서 "user_tel"을 기대
        } catch (JSONException e) {
            Toast.makeText(this, "요청 데이터 생성 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        // API 호출
        HttpUtils.sendJsonToServerWithAuth(requestData, "/findUserPw", null, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String password = response.getString("password");
                            navigateToPwRecoveryScreen(password);
                        } else {
                            String message = response.optString("message", "비밀번호를 찾을 수 없습니다.");
                            navigateToPwNotFoundScreen(message);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(FindIDPWActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(FindIDPWActivity.this, "요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });

    }



    // 성공적으로 비밀번호를 찾았을 때
    private void navigateToPwRecoveryScreen(String password) {
        Intent intent = new Intent(this, PwRecoveryActivity.class);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    // 비밀번호를 찾지 못했을 때
    private void navigateToPwNotFoundScreen(String message) {
        Intent intent = new Intent(this, PwNotFoundActivity.class);
        intent.putExtra("errorMessage", message);
        startActivity(intent);
    }
}
