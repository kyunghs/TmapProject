package com.myapplication.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;

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

    // 추가된 변수: 시간과 분을 분으로 변환해서 저장
    private int totalMinutes = 0;

    private String selectedBenefit = "선택사항 없음";

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
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogBorder20WhiteTheme);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.park_list, container, false);

        // 스피너 초기화
        Spinner benefitSpinner = view.findViewById(R.id.benefitSpinner);
        ArrayAdapter<CharSequence> benefitAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.benefit_options, android.R.layout.simple_spinner_item);
        benefitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        benefitSpinner.setAdapter(benefitAdapter);

        // 스피너 항목 선택 리스너 설정
        benefitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedBenefit = (String) parentView.getItemAtPosition(position);
                Log.e("Spinner", "선택된 혜택: " + selectedBenefit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // 시간 및 분 스피너 초기화
        Spinner hourSpinner = view.findViewById(R.id.hourSpinner);
        Spinner minuteSpinner = view.findViewById(R.id.minuteSpinner);

        // 시간 스피너 설정
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.hour_options, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        // 분 스피너 설정
        ArrayAdapter<CharSequence> minuteAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.minute_options, android.R.layout.simple_spinner_item);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        // 스피너 항목 선택 리스너 설정 (시간 및 분)
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateTotalMinutes(hourSpinner, minuteSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        minuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateTotalMinutes(hourSpinner, minuteSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // RecyclerView 초기화
        RecyclerView parkingRecyclerView = view.findViewById(R.id.parkingRecyclerView);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // JSON 데이터를 파싱하여 parkingList를 초기화
        if (getArguments() != null) {
            String parkData = getArguments().getString(ARG_PARK_DATA);
            parseParkData(parkData);
        }

        // 기존 adapter 변수명 그대로 사용
        ParkingAdapter adapter = new ParkingAdapter(parkingList, (name, lat, lot) -> {
            Intent intent = new Intent(requireContext(), DriveActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lot", lot);
            startActivity(intent);
        });
        parkingRecyclerView.setAdapter(adapter);
        // 정렬 기준 스위칭 추가
        View sortByButton = view.findViewById(R.id.sortBy);
        final String[] currentSortBy = {"distance"}; // 초기 정렬 기준: 거리순

        sortByButton.setOnClickListener(v -> {
            if (currentSortBy[0].equals("distance")) {
                currentSortBy[0] = "fee";
                ((Button) sortByButton).setText("요금순");
                sortParkingListByFee();
            } else {
                currentSortBy[0] = "distance";
                ((Button) sortByButton).setText("거리순");
                sortParkingListByDistance();
            }
            adapter.notifyDataSetChanged(); // RecyclerView 업데이트
        });

        return view;
    }

    // 거리순 정렬 메서드
    private void sortParkingListByDistance() {
        parkingList.sort((p1, p2) -> {
            int distance1 = extractNumber(p1.getDistance());
            int distance2 = extractNumber(p2.getDistance());
            return Integer.compare(distance1, distance2);
        });
    }

    // 요금순 정렬 메서드
    private void sortParkingListByFee() {
        parkingList.sort((p1, p2) -> {
            double fee1 = Double.parseDouble(p1.getTotalFee().replaceAll("[^\\d.]", ""));
            double fee2 = Double.parseDouble(p2.getTotalFee().replaceAll("[^\\d.]", ""));
            return Double.compare(fee1, fee2);
        });
    }

    // 거리 텍스트에서 숫자를 추출하는 메서드
    private int extractNumber(String text) {
        String number = text.replaceAll("[^\\d]", "");
        return number.isEmpty() ? 0 : Integer.parseInt(number);

    }


    // 시간 및 분을 분으로 변환하여 totalMinutes에 저장
    private void updateTotalMinutes(Spinner hourSpinner, Spinner minuteSpinner) {
        int hour = Integer.parseInt(hourSpinner.getSelectedItem().toString());
        int minute = Integer.parseInt(minuteSpinner.getSelectedItem().toString());

        // 총 분 계산
        totalMinutes = (hour * 60) + minute;

        // 로그로 확인
        Log.e("Time Conversion", "선택된 시간: " + hour + "시간 " + minute + "분 -> 총 " + totalMinutes + "분");
    }

    private void parseParkData(String parkData) {
        try {
            JSONObject rootObject = new JSONObject(parkData);
            JSONArray parkArray = rootObject.getJSONArray("parks");
            for (int i = 0; i < parkArray.length(); i++) {
                JSONObject parkObject = parkArray.getJSONObject(i);
                String name = parkObject.optString("name", "Unknown");
                String remain = parkObject.optString("now_prk_vhcl_cnt", "0").trim();
                remain = remain.isEmpty() ? "0" : remain;

                String baseFee = parkObject.optString("bsc_prk_crg", "0");
                String addFee = parkObject.optString("add_prk_crg", "0");
                String dayMaxFee = parkObject.optString("day_max_crg", "0");
                String lat = parkObject.optString("lat", "0");
                String lot = parkObject.optString("lot", "0");

                double bscPrkCrg = 0.0, addPrkCrg = 0.0, dayMaxCrg = 0.0, targetLat = 0.0, targetLot = 0.0;
                try {
                    bscPrkCrg = Double.parseDouble(baseFee);
                    addPrkCrg = Double.parseDouble(addFee);
                    dayMaxCrg = Double.parseDouble(dayMaxFee);
                    targetLat = Double.parseDouble(lat);
                    targetLot = Double.parseDouble(lot);
                } catch (NumberFormatException e) {
                    Log.e("parseParkData", "Invalid number format: baseFee=" + baseFee +
                            ", addFee=" + addFee + ", dayMaxFee=" + dayMaxFee +
                            ", lat=" + lat + ", lot=" + lot);
                }

                Location currentLocation = SDKManager.getInstance().getCurrentPosition();
                double currentLong = currentLocation.getLongitude();
                double currentLat = currentLocation.getLatitude();

                String totalFee = Utils.calculateParkingFee(bscPrkCrg, addPrkCrg, dayMaxCrg, 120);

                String distance = parkObject.optString("distance", "0m").replaceAll("[^\\d]", "");
                distance = distance.isEmpty() ? "0" : distance;
                distance = Integer.parseInt(distance) >= 1000
                        ? String.format("%.1fkm", Integer.parseInt(distance) / 1000.0)
                        : NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(distance)) + "m";

                int[] totalTime = {0};
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
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "JSON 파싱 오류: totalTime", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getActivity(), "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

                parkingList.add(new Parking(name, remain, distance, totalFee, lat, lot, totalTime[0]));
                Log.e("ParkingList", "Added: " + name + ", remain: " + remain);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}