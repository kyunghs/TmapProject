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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkListBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_PARK_DATA = "park_data";
    private List<Parking> parkingList = new ArrayList<>();

    public static ParkListBottomSheetFragment newInstance(String parkData) {
        ParkListBottomSheetFragment fragment = new ParkListBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARK_DATA, parkData);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.park_list, container, false);

        // RecyclerView 초기화
        RecyclerView parkingRecyclerView = view.findViewById(R.id.parkingRecyclerView);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // JSON 데이터를 파싱하여 parkingList를 초기화
        if (getArguments() != null) {
            String parkData = getArguments().getString(ARG_PARK_DATA);
            parseParkData(parkData);
        }

        // 어댑터 설정
        ParkingAdapter adapter = new ParkingAdapter(parkingList);
        parkingRecyclerView.setAdapter(adapter);

        return view;
    }

    private void parseParkData(String parkData) {
        try {
            JSONObject rootObject = new JSONObject(parkData);
            JSONArray parkArray = rootObject.getJSONArray("parks");
            for (int i = 0; i < parkArray.length(); i++) {
                JSONObject parkObject = parkArray.getJSONObject(i);
                String name = parkObject.optString("name", "Unknown");
                String distance = parkObject.optString("distance", "0m");
                distance = distance.replaceAll("[^\\d]", "").isEmpty() ? "0" : distance.replaceAll("[^\\d]", "");
                distance = Integer.parseInt(distance) >= 1000
                        ? String.format("%.1fkm", Integer.parseInt(distance) / 1000.0)
                        : NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(distance)) + "m";

                String price = parkObject.optString("price", "0원");
                String availability = parkObject.optString("availability", "알 수 없음");

                // Parking 객체로 변환하여 리스트에 추가
                parkingList.add(new Parking(name, distance, price, availability));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
