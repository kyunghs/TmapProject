package com.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapplication.R;
import com.myapplication.UserEditActivity;

public class UserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // "프로필 편집" 버튼 초기화
        Button editProfileButton = view.findViewById(R.id.edit_profile_button); // 버튼 ID를 fragment_user.xml에서 설정

        // 클릭 이벤트 설정
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // UserEditActivity로 이동
                Intent intent = new Intent(getActivity(), UserEditActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
