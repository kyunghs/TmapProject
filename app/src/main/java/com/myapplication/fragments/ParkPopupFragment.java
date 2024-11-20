package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;

public class ParkPopupFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.park_popup, container, false);

        // "아니요" 버튼 클릭 리스너 추가
        Button noButton = view.findViewById(R.id.noBtn);
        noButton.setOnClickListener(v -> {
            // 현재 팝업 닫기
            dismiss();

            // BottomSheetFragment 닫기
            FragmentManager fragmentManager = getParentFragmentManager();
            BottomSheetDialogFragment bottomSheetFragment =
                    (BottomSheetDialogFragment) fragmentManager.findFragmentByTag("BottomSheetFragment");
            if (bottomSheetFragment != null) {
                bottomSheetFragment.dismiss(); // BottomSheet 닫기
            }

            // map_bottom.xml 표시
            if (getActivity() != null) {
                ViewGroup rootView = getActivity().findViewById(android.R.id.content);
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View mapBottomView = layoutInflater.inflate(R.layout.map_bottom, rootView, false);
                rootView.addView(mapBottomView); // map_bottom.xml 추가
            }
        });

        return view;
    }
}
