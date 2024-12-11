package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;
import com.myapplication.utils.HttpSearchUtils;

public class BookmarkEditBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmark_edit_bottom_sheet, container, false);

        // EditText 초기화
        EditText editTextNickname = view.findViewById(R.id.editTextNickname);

        // 키보드의 확인 버튼 이벤트 처리
        editTextNickname.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 확인 버튼 눌렀을 때 동작
                Toast.makeText(getActivity(), "확인 버튼 눌림@@: " + editTextNickname.getText().toString(), Toast.LENGTH_SHORT).show();

                // 필요한 추가 로직 (예: 서버로 데이터 전송 등)ㅣ

                return true; // 이벤트 처리 완료
            }
            return false;
        });

        // EditorActionListener 설정
        editTextNickname.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String inputText = editTextNickname.getText().toString();
                HttpSearchUtils.performSearch(inputText, requireContext(), getParentFragmentManager());
                return true;
            }
            return false;
        });

        return view;
    }
}
