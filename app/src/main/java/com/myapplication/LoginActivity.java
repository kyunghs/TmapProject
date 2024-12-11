package com.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
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
    private ProgressDialog progressDialog;

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

        editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(v -> {
            // 사용자 입력값 가져오기
            String id = editTextId.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!isNetworkAvailable()) {
                Toast.makeText(LoginActivity.this, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 입력값 검증
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 로딩 표시
            showProgressDialog("로그인 중...");

            // JSON 데이터 생성
            JSONObject loginData = new JSONObject();
            try {
                loginData.put("id", id);
                loginData.put("password", password);
            } catch (JSONException e) {
                Log.e(TAG, "JSON 생성 오류: " + e.getMessage());
                dismissProgressDialog();
                Toast.makeText(LoginActivity.this, "로그인 요청 생성 중 오류 발생", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버 요청
            HttpUtils.sendJsonToServer(loginData, "/Login", new HttpUtils.HttpResponseCallback() {
                @Override
                public void onSuccess(JSONObject responseData) {
                    dismissProgressDialog();

                    // 서버 응답 디버깅 로그 추가
                    Log.d("LoginActivity", "응답 데이터: " + responseData.toString());

                    try {
                        boolean loginSuccess = responseData.getBoolean("success");
                        if (loginSuccess) {
                            String token = responseData.getString("token");
                            saveToken(token);

                            // MainActivity로 이동
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
                    dismissProgressDialog();
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

    // 로딩 다이얼로그 표시
    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    // 로딩 다이얼로그 닫기
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // 토큰 저장 함수
    private void saveToken(String token) {
        getSharedPreferences("AuthPrefs", MODE_PRIVATE)  // 기존 키와 통일
                .edit()
                .putString("auth_token", token)          // 토큰 키 통일
                .apply();
        Log.d(TAG, "토큰 저장 완료: " + token);             // 디버깅 로그
    }

    // 네트워크 상태 확인 함수
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
