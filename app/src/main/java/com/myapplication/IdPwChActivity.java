package com.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class IdPwChActivity extends AppCompatActivity {

    private static final String TAG = "IdPwChActivity";
    private EditText inputName, inputPhone;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_pw_change);

        inputName = findViewById(R.id.input_name);
        inputPhone = findViewById(R.id.input_phone);
        actionButton = findViewById(R.id.action_button);

        actionButton.setOnClickListener(v -> findUserId());
    }

    private void findUserId() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "이름과 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("name", name);
            requestData.put("phone", phone);
        } catch (JSONException e) {
            Log.e(TAG, "JSON 생성 오류: " + e.getMessage());
            Toast.makeText(this, "요청 데이터 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.sendJsonToServer(requestData, "/findUserId", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                runOnUiThread(() -> {
                    try {
                        boolean success = responseData.getBoolean("success");
                        if (success) {
                            String userId = responseData.getString("userId");
                            Toast.makeText(IdPwChActivity.this, "아이디: " + userId, Toast.LENGTH_LONG).show();
                        } else {
                            String message = responseData.optString("message", "아이디를 찾을 수 없습니다.");
                            Toast.makeText(IdPwChActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "응답 파싱 오류: " + e.getMessage());
                        Toast.makeText(IdPwChActivity.this, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(IdPwChActivity.this, "요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
