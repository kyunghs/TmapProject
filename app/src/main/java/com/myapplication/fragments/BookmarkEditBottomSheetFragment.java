package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;

public class BookmarkEditBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // bookmark_edit_bottom_sheet.xml 레이아웃 연결
        return inflater.inflate(R.layout.bookmark_edit_bottom_sheet, container, false);
    }
}
