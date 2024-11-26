package com.myapplication.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;
import com.myapplication.adapters.PlaceAdapter;
import com.myapplication.models.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.places_list, container, false);

        // RecyclerView 초기화
        RecyclerView placesRecyclerView = view.findViewById(R.id.placesRecyclerView);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 장소 리스트 초기화
        List<Place> placeList = new ArrayList<>();
        placeList.add(new Place("서울특별시 중구 세종대로 110"));
        placeList.add(new Place("서울특별시 강남구 테헤란로 123"));
        placeList.add(new Place("서울특별시 서초구 서초대로 123"));
        placeList.add(new Place("서울특별시 마포구 월드컵북로 240"));

        // 어댑터 설정
        PlaceAdapter adapter = new PlaceAdapter(placeList, this::showPopup);
        placesRecyclerView.setAdapter(adapter);

        return view;
    }

    // 팝업을 표시하는 메서드
    private void showPopup(Place place) {
        // park_popup.xml을 inflate한 Dialog 생성
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.park_popup);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // "예" 버튼 클릭 이벤트
        dialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            // park_list.xml을 표시하는 ParkListBottomSheetFragment 호출
            ParkListBottomSheetFragment parkListBottomSheet = new ParkListBottomSheetFragment();
            parkListBottomSheet.show(getParentFragmentManager(), "ParkListBottomSheetFragment");

            dialog.dismiss(); // 다이얼로그 닫기
        });

        // "아니요" 버튼 클릭 이벤트
        dialog.findViewById(R.id.noBtn).setOnClickListener(v -> {
            // PathSelectBottomSheetFragment 표시
            PathSelectBottomSheetFragment pathSelectBottomSheetFragment = new PathSelectBottomSheetFragment();
            pathSelectBottomSheetFragment.show(getParentFragmentManager(), "PathSelectBottomSheetFragment");

            dialog.dismiss(); // 현재 다이얼로그 닫기
        });

        dialog.show();
    }
}
