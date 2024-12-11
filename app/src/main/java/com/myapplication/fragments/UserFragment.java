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
import android.widget.LinearLayout;
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

    private String token; // 토큰을 멤버 변수로 선언
    private TextView nameText;
    private ImageView profileImageView;

    private TextView parkingDiscountDescription;
    private LinearLayout currentlySelectedLayout = null;

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final String PROFILE_IMAGE_NAME = "profile_image.png";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // 설명 텍스트 초기화
        parkingDiscountDescription = view.findViewById(R.id.parking_discount_description);

        // 항목 클릭 이벤트 설정
        setupGridItemListeners(view);

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

    // Grid 아이템 클릭 리스너 설정
    private void setupGridItemListeners(View view) {
        LinearLayout disabledPerson = view.findViewById(R.id.disabled_persons);
        LinearLayout multipleChildren = view.findViewById(R.id.multiple_children);
        LinearLayout lowEmissionVehicles = view.findViewById(R.id.low_emission_vehicles);
        LinearLayout personOfNationalMerit = view.findViewById(R.id.person_of_national_merit);
        LinearLayout modelTaxpayer = view.findViewById(R.id.model_taxpayer);
        LinearLayout singleParentFamily = view.findViewById(R.id.single_parent_family);

        // 각 항목 클릭 이벤트 등록
        setupToggleBackground(disabledPerson, "장애인: 80% 감면 (지하철 환승주차장에 한하여 최초 3시간 주차요금 면제 후 80% 감면)", R.drawable.disabled_persons);
        setupToggleBackground(multipleChildren, "다자녀(2자녀 이상): 50% 감면", R.drawable.multiple_children);
        setupToggleBackground(lowEmissionVehicles, "저공해 차량: 50% 감면 + 전기차 충전 시 1시간 면제 후 50% 감면", R.drawable.low_emission_vehicles);
        setupToggleBackground(personOfNationalMerit, "국가유공자: 80% 감면", R.drawable.person_of_national_merit);
        setupToggleBackground(modelTaxpayer, "모범납세자: 1년간 주차요금 면제", R.drawable.model_taxpayer);
        setupToggleBackground(singleParentFamily, "한부모 가정: 50% 감면", R.drawable.single_parent_family);
    }

    // 배경 토글 로직 (한 번에 하나의 항목만 활성화 + 설명 표시)
    private void setupToggleBackground(LinearLayout layout, String description, int drawableId) {
        ImageView imageView = (ImageView) layout.getChildAt(0); // 첫 번째 자식이 ImageView
        TextView textView = (TextView) layout.getChildAt(1);    // 두 번째 자식이 TextView

        layout.setOnClickListener(v -> {
            if (imageView == null || textView == null) {
                Log.e("UserFragment", "ImageView 또는 TextView를 찾을 수 없습니다!");
                return;
            }

            // 이전 선택된 항목 초기화
            if (currentlySelectedLayout != null && currentlySelectedLayout != layout) {
                resetLayout(currentlySelectedLayout);
            }

            // 현재 항목 활성화 또는 비활성화
            Object tag = layout.getTag();
            if (tag != null && (boolean) tag) {
                // 기존 상태로 복구
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                layout.setBackgroundResource(R.drawable.border);
                layout.setTag(false);

                // 설명 숨김
                parkingDiscountDescription.setVisibility(View.GONE);

                // 현재 선택된 항목 초기화
                currentlySelectedLayout = null;

                // API 호출로 선택 해제 상태 업데이트
                sendSelectionToServer(null);
            } else {
                // 새로운 상태로 변경
                imageView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                layout.setBackgroundResource(drawableId);
                layout.setTag(true);

                // 설명 업데이트
                parkingDiscountDescription.setText(description);
                parkingDiscountDescription.setVisibility(View.VISIBLE);

                // 현재 선택된 항목 업데이트
                currentlySelectedLayout = layout;

                // API 호출로 선택 상태 업데이트
                sendSelectionToServer(layout.getId());
            }
        });
    }

    // 서버로 선택 상태 전송
    private void sendSelectionToServer(Integer selectedLayoutId) {
        String selectedColumn = null;

        if (selectedLayoutId != null) {
            switch (selectedLayoutId) {
                case R.id.disabled_persons:
                    selectedColumn = "disabled_human";
                    break;
                case R.id.multiple_children:
                    selectedColumn = "multiple_child";
                    break;
                case R.id.low_emission_vehicles:
                    selectedColumn = "electric_car";
                    break;
                case R.id.person_of_national_merit:
                    selectedColumn = "person_merit";
                    break;
                case R.id.model_taxpayer:
                    selectedColumn = "tax_payment";
                    break;
                case R.id.single_parent_family:
                    selectedColumn = "alone_family";
                    break;
            }
        }

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("selected_column", selectedColumn);

            // 요청 전송
            HttpUtils.sendJsonToServerWithAuth(requestData, "/updateUserSelection", token, new HttpUtils.HttpResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("UserFragment", "선택 상태 업데이트 성공: " + response.toString());
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("UserFragment", "선택 상태 업데이트 실패: " + errorMessage);
                }
            });
        } catch (JSONException e) {
            Log.e("UserFragment", "JSON 생성 오류: " + e.getMessage());
        }
    }



    // 레이아웃 초기화 메서드
    private void resetLayout(LinearLayout layout) {
        ImageView imageView = (ImageView) layout.getChildAt(0); // 첫 번째 자식이 ImageView
        TextView textView = (TextView) layout.getChildAt(1);    // 두 번째 자식이 TextView

        if (imageView != null) imageView.setVisibility(View.VISIBLE);
        if (textView != null) textView.setVisibility(View.VISIBLE);
        layout.setBackgroundResource(R.drawable.border); // 기본 배경으로 복구
        layout.setTag(false);
    }

    // 유저 정보 가져오기
    private void fetchUserInfo() {
        token = "Bearer " + getActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token.isEmpty()) {
            Log.e("UserFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.sendGetRequestWithAuth("/getUserInfo", token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                getActivity().runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.optJSONObject("data");
                            if (userData != null) {
                                String name = userData.optString("name", "알 수 없음");
                                nameText.setText(name);

                                // 초기 선택 상태 설정
                                updateGridSelectionFromData(userData);
                            } else {
                                Toast.makeText(getActivity(), "유효한 사용자 데이터를 받을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), responseData.optString("message", "오류 발생"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "데이터 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    // 서버 데이터 기반으로 Grid 초기화
    private void updateGridSelectionFromData(JSONObject userData) {
        if (userData.optString("disabled_human", "N").equals("Y")) {
            setSelectedLayout(R.id.disabled_persons);
        } else if (userData.optString("multiple_child", "N").equals("Y")) {
            setSelectedLayout(R.id.multiple_children);
        } else if (userData.optString("electric_car", "N").equals("Y")) {
            setSelectedLayout(R.id.low_emission_vehicles);
        } else if (userData.optString("person_merit", "N").equals("Y")) {
            setSelectedLayout(R.id.person_of_national_merit);
        } else if (userData.optString("tax_payment", "N").equals("Y")) {
            setSelectedLayout(R.id.model_taxpayer);
        } else if (userData.optString("alone_family", "N").equals("Y")) {
            setSelectedLayout(R.id.single_parent_family);
        }
    }

    // Grid 항목 선택 상태 설정
    private void setSelectedLayout(int layoutId) {
        LinearLayout layout = getView().findViewById(layoutId);
        if (layout != null) {
            setupToggleBackground(layout, "", 0); // 임시 데이터로 호출
            layout.performClick(); // 클릭 이벤트 트리거
        }
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
