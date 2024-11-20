package com.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit);

        // 저장 버튼 클릭 이벤트
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            // 변경 사항 저장 로직 추가
            Toast.makeText(this, "변경사항이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료
        });

        // 취소 버튼 클릭 이벤트
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            // 취소 동작
            Toast.makeText(this, "편집이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료
        });
    }
}
