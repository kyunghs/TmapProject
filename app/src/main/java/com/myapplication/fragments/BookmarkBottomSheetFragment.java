package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;

public class BookmarkBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // bookmark_bottom_sheet.xml 레이아웃 연결
        View view = inflater.inflate(R.layout.bookmark_bottom_sheet, container, false);

        // ImageView 클릭 이벤트 처리
        ImageView editIcon = view.findViewById(R.id.editIcon);
        editIcon.setOnClickListener(v -> {
            // BookmarkEditBottomSheetFragment 표시
            FragmentManager fragmentManager = getParentFragmentManager();
            BookmarkEditBottomSheetFragment editBottomSheet = new BookmarkEditBottomSheetFragment();
            editBottomSheet.setStyle(
                    BottomSheetDialogFragment.STYLE_NORMAL,
                    R.style.AppBottomSheetDialogBorder20WhiteTheme
            );
            editBottomSheet.show(fragmentManager, editBottomSheet.getTag());
        });

        return view;
    }
}
