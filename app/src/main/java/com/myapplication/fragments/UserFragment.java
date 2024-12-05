package com.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapplication.R;
import com.myapplication.UserEditActivity;
import com.myapplication.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserFragment extends Fragment {

    private TextView nameText;
    private ImageView profileImageView;

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final String PROFILE_IMAGE_NAME = "profile_image.png";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // UI 요소 초기화
        nameText = view.findViewById(R.id.name_text);
        profileImageView = view.findViewById(R.id.profile_image);
        Button editProfileButton = view.findViewById(R.id.edit_profile_button);

        // 로컬 저장소에서 프로필 이미지 로드
        loadImageFromLocalStorage();

        // 유저 정보 로드
        fetchUserInfo();

        // 프로필 편집 버튼 클릭 이벤트
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserEditActivity.class);
            startActivity(intent);
        });

        // 프로필 이미지 클릭 이벤트
        profileImageView.setOnClickListener(v -> openGallery());

        return view;
    }

    // 유저 정보 가져오기
    private void fetchUserInfo() {
        String token = "Bearer " + getActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 요청으로 사용자 정보 가져오기
        HttpUtils.sendGetRequestWithAuth("/getUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        Log.d("UserFragment", "서버 응답: " + responseData.toString());

                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.optJSONObject("data");
                            if (userData != null) {
                                String name = userData.optString("name", "알 수 없음");

                                // UI 업데이트
                                nameText.setText(name);

                                Log.d("UserFragment", "UI 업데이트 완료 - 이름: " + name);
                            } else {
                                Toast.makeText(getActivity(), "유효한 사용자 데이터를 받을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = responseData.optString("message", "오류 발생");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "데이터 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserFragment", "JSONException", e);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("UserFragment", "요청 실패: " + errorMessage);
                });
            }
        });
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 이미지 선택 결과 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri); // 이미지 설정
                profileImageView.setClipToOutline(true); // 원형 유지
                saveImageToLocalStorage(selectedImageUri); // 이미지 로컬 저장
                Log.d("UserFragment", "이미지 업데이트 완료: " + selectedImageUri.toString());
                Toast.makeText(getActivity(), "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 이미지 로컬 저장
    private void saveImageToLocalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 이미지 저장 경로
            File file = new File(getActivity().getFilesDir(), PROFILE_IMAGE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Log.d("UserFragment", "이미지 로컬 저장 완료: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "이미지를 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 로컬 저장소에서 이미지 로드
    private void loadImageFromLocalStorage() {
        File file = new File(getActivity().getFilesDir(), PROFILE_IMAGE_NAME);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
            profileImageView.setClipToOutline(true); // 원형 유지
        }
    }
}
