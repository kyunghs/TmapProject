package com.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.myapplication.R;
import com.myapplication.utils.HttpSearchUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;

public class HomeFragment extends Fragment {
    private final static String CLIENT_ID = "";
    private final static String API_KEY = "qfhtGmuYyk3bKgfAwRxra5UIpzImSFxU9Wg1uWlp";
    private final static String USER_KEY = "";
    private final static String DEVICE_KEY = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Tmapinit();
        // "집으로" 카드 레이아웃 초기화
        LinearLayout homeCardLayout = view.findViewById(R.id.home_card_layout);
        EditText searchHome = view.findViewById(R.id.search_home);

        // 클릭 이벤트 설정
        homeCardLayout.setClickable(true);
        homeCardLayout.setFocusable(true);
        homeCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // BookmarkBottomSheetFragment 호출 및 스타일 지정
                BookmarkBottomSheetFragment bottomSheet = new BookmarkBottomSheetFragment();
                bottomSheet.setStyle(
                        BottomSheetDialogFragment.STYLE_NORMAL,
                        R.style.AppBottomSheetDialogBorder20WhiteTheme
                );
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                bottomSheet.show(fragmentManager, bottomSheet.getTag());
            }
        });

        //목적지 검색 시
        searchHome.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String inputText = searchHome.getText().toString();
                HttpSearchUtils.performSearch(inputText, requireContext(), getParentFragmentManager());
                return true;
            }
            return false;
        });


        return view;
    }

    public void Tmapinit(){
        TmapUISDK.Companion.initialize(getActivity(), CLIENT_ID, API_KEY, USER_KEY, DEVICE_KEY, new TmapUISDK.InitializeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "#!@#!@#I@!FJSDFdwsjfiowejfiowejfew", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int i, @Nullable String s) {
            }

            @Override
            public void savedRouteInfoExists(@Nullable String dest) {
            }
        });
    }
}
