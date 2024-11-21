package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.MainActivity;
import com.myapplication.R;

public class PathSelectBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.path_select_sheet, container, false);

        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            // MainActivity의 showMapBottomView() 호출
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMapBottomView();
            }

            dismiss(); // 현재 BottomSheet 닫기
        });

        return view;
    }
}
