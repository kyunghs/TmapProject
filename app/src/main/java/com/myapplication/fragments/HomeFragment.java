package com.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.myapplication.DriveActivity;
import com.myapplication.R;
import com.myapplication.fragments.BookmarkBottomSheetFragment;
import com.myapplication.utils.HttpSearchUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // "집으로" 카드 레이아웃 초기화
        LinearLayout homeCardLayout = view.findViewById(R.id.home_card_layout);
        EditText searchHome = view.findViewById(R.id.search_home);

        // 클릭 이벤트 설정
        homeCardLayout.setClickable(true);
        homeCardLayout.setFocusable(true);
        homeCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // BookmarkBottomSheetFragment 호출
                BookmarkBottomSheetFragment bottomSheet = new BookmarkBottomSheetFragment();
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
}
