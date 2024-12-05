package com.myapplication.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.DriveActivity;
import com.myapplication.R;
import com.myapplication.adapters.ParkingAdapter;
import com.myapplication.models.Parking;
import com.myapplication.utils.HttpSearchUtils;
import com.myapplication.utils.Utils;
import com.skt.tmap.engine.navigation.SDKManager;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 바텀시트 스타일 설정
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogBorder20WhiteTheme);
    }

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
        ParkingAdapter adapter = new ParkingAdapter(parkingList, (name, lat, lot) -> {
            // 클릭 이벤트 처리
            Intent intent = new Intent(requireContext(), DriveActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lot", lot);
            startActivity(intent);
        });
        parkingRecyclerView.setAdapter(adapter);

        return view;
    }

    private void parseParkData(String parkData) {
        try {
            JSONObject rootObject = new JSONObject(parkData);
            JSONArray parkArray = rootObject.getJSONArray("parks");

            for (int i = 0; i < parkArray.length(); i++) {
                JSONObject parkObject = parkArray.getJSONObject(i);

                // 필수 필드 초기화 및 null/empty 처리
                String name = parkObject.optString("name", "Unknown");
                String remain = parkObject.optString("now_prk_vhcl_cnt", "").trim();
                remain = remain.isEmpty() ? "0" : remain;

                String baseFee = parkObject.optString("bsc_prk_crg", "0");
                String addFee = parkObject.optString("add_prk_crg", "0");
                String dayMaxFee = parkObject.optString("day_max_crg", "0");

                String lat = parkObject.optString("lat", "0.0");
                String lot = parkObject.optString("lot", "0.0");

                double targetLat, targetLot;
                try {
                    targetLat = Double.parseDouble(lat);
                    targetLot = Double.parseDouble(lot);
                } catch (NumberFormatException e) {
                    Log.e("ParseParkData", "Invalid lat/lot values: " + lat + ", " + lot);
                    targetLat = 0.0;
                    targetLot = 0.0;
                }

                Location currentLocation = SDKManager.getInstance().getCurrentPosition();
                double currentLong = currentLocation.getLongitude();
                double currentLat = currentLocation.getLatitude();

                // 주차 요금 계산
                double bscPrkCrg, addPrkCrg, dayMaxCrg;
                try {
                    bscPrkCrg = Double.parseDouble(baseFee);
                    addPrkCrg = Double.parseDouble(addFee);
                    dayMaxCrg = Double.parseDouble(dayMaxFee);
                } catch (NumberFormatException e) {
                    Log.e("ParseParkData", "Invalid fee values: " + baseFee + ", " + addFee + ", " + dayMaxFee);
                    bscPrkCrg = addPrkCrg = dayMaxCrg = 0.0;
                }

                int parkingTime = 120; // 하드코딩된 주차 시간
                String totalFee = Utils.calculateParkingFee(bscPrkCrg, addPrkCrg, dayMaxCrg, parkingTime);

                // 거리 계산
                String distance = parkObject.optString("distance", "0m").trim();
                distance = distance.replaceAll("[^\\d]", "").isEmpty() ? "0" : distance.replaceAll("[^\\d]", "");
                try {
                    int distValue = Integer.parseInt(distance);
                    distance = distValue >= 1000
                            ? String.format("%.1fkm", distValue / 1000.0)
                            : NumberFormat.getNumberInstance(Locale.US).format(distValue) + "m";
                } catch (NumberFormatException e) {
                    Log.e("ParseParkData", "Invalid distance value: " + distance);
                    distance = "0m";
                }

                // 초기 totalTime 값을 0으로 설정
                int[] totalTime = {0};

                // 비동기 요청
                HttpSearchUtils.performRouteRequest(requireContext(), 0, currentLong, currentLat, targetLot, targetLat, new HttpSearchUtils.RouteRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject rootObject) {
                        try {
                            if (rootObject.has("features")) {
                                JSONArray features = rootObject.getJSONArray("features");
                                if (features.length() > 0) {
                                    JSONObject firstFeature = features.getJSONObject(0);
                                    if (firstFeature.has("properties")) {
                                        JSONObject properties = firstFeature.getJSONObject("properties");
                                        if (properties.has("totalTime")) {
                                            totalTime[0] = properties.getInt("totalTime");
                                        }
                                    }
                                }
                            }
                            Log.e("RouteRequest", "Total Time for " + name + ": " + totalTime[0]);
                        } catch (JSONException e) {
                            Log.e("RouteRequest", "JSON 파싱 오류: totalTime", e);
                            Toast.makeText(getActivity(), "JSON 파싱 오류: totalTime", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("RouteRequest", "오류: " + errorMessage);
                        Toast.makeText(getActivity(), "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

                // Parking 객체 생성 및 추가
                parkingList.add(new Parking(name, remain, distance, totalFee, lat, lot, totalTime[0]));
                Log.e("ParkingList", "Added: " + name + ", remain: " + remain);
                Log.e("ParkingList", "Added: " + name + ", Total Time: " + totalTime[0]);
            }
        } catch (JSONException e) {
            Log.e("ParseParkData", "JSON 파싱 오류", e);
        }
    }
}
