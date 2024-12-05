package com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final String PROFILE_IMAGE_NAME = "profile_image_edit.png";

    private EditText nameField, phoneField, passwordField;
    private ImageView profileImageView;
    private String currentName, currentPhone, currentPassword; // 현재 사용자 정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.user_edit);

        // UI 초기화
        nameField = findViewById(R.id.name_field);
        phoneField = findViewById(R.id.phone_field);
        passwordField = findViewById(R.id.password_field);
        profileImageView = findViewById(R.id.profile_image_select);

        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        // 프로필 이미지 로드
        loadImageFromLocalStorage();

        // 사용자 데이터 가져오기
        fetchUserData();

        // 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener(v -> saveUserData());

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener(v -> finish());

        // 프로필 이미지 클릭 이벤트
        profileImageView.setOnClickListener(v -> openGallery());
    }

    private void fetchUserData() {
        // 서버 요청 코드 생략 (기존 코드 유지)
    }

    private void saveUserData() {
        // 서버 저장 요청 코드 생략 (기존 코드 유지)
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 이미지 선택 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri); // 이미지 설정
                saveImageToLocalStorage(selectedImageUri); // 로컬 저장
            }
        }
    }

    // 이미지 로컬 저장
    private void saveImageToLocalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 이미지 저장 경로
            File file = new File(getFilesDir(), PROFILE_IMAGE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Log.d("UserEditActivity", "이미지 로컬 저장 완료: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지를 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 로컬 저장소에서 이미지 로드
    private void loadImageFromLocalStorage() {
        File file = new File(getFilesDir(), PROFILE_IMAGE_NAME);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
            profileImageView.setClipToOutline(true); // 원형 유지
        }
    }
}
