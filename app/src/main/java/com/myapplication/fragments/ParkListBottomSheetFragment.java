package com.myapplication.fragments;

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
import com.myapplication.adapters.ParkingAdapter;
import com.myapplication.models.Parking;

import java.util.ArrayList;
import java.util.List;

public class ParkListBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.park_list, container, false);

        // RecyclerView 초기화
        RecyclerView parkingRecyclerView = view.findViewById(R.id.parkingRecyclerView);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 주차장 데이터 초기화
        List<Parking> parkingList = new ArrayList<>();
        parkingList.add(new Parking("주차장_1", "50m", "6,000원", "남은 자리: 5"));
        parkingList.add(new Parking("주차장_2", "100m", "5,000원", "남은 자리: 3"));
        parkingList.add(new Parking("주차장_3", "150m", "4,500원", "남은 자리: 2"));

        // 어댑터 설정
        ParkingAdapter adapter = new ParkingAdapter(parkingList);
        parkingRecyclerView.setAdapter(adapter);

        return view;
    }
}
