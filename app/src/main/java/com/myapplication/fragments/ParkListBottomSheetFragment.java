package com.myapplication.fragments;

import android.content.Context;
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
import com.myapplication.utils.HttpUtils;
import com.myapplication.utils.Utils;
import com.skt.tmap.engine.navigation.SDKManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParkListBottomSheetFragment extends BottomSheetDialogFragment {

    private String token;


    private static final String ARG_PARK_DATA = "park_data";
    private List<Parking> parkingList = new ArrayList<>();

    // 추가된 변수: 시간과 분을 분으로 변환해서 저장
    private int totalMinutes = 120;  // 주차 시간 (분 단위)
    private String selectedBenefit = "선택사항 없음";

    public static ParkListBottomSheetFragment newInstance(String parkData) {
        ParkListBottomSheetFragment fragment = new ParkListBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARK_DATA, parkData);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.park_list, container, false);






        // SharedPreferences에서 인증 토큰 가져오기
        token = requireActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token == null || token.isEmpty()) {
            Log.e("ParkListFragment", "토큰이 없습니다. 로그인이 필요합니다.");
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Spinner 초기화
        Spinner benefitSpinner = view.findViewById(R.id.benefitSpinner);
        ArrayAdapter<CharSequence> benefitAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.benefit_options, android.R.layout.simple_spinner_item);
        benefitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        benefitSpinner.setAdapter(benefitAdapter);

        // 사용자 데이터를 기반으로 스피너 설정
        fetchUserDataAndSetupSpinner(benefitSpinner);

        // 스피너 항목 선택 리스너 설정 (혜택)
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

        initializeTimeSpinners(view);
        initializeRecyclerView(view);


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

        // 기본값 설정 (2시간 0분)
        hourSpinner.setSelection(2);  // 2시간
        minuteSpinner.setSelection(0);  // 0분

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

    private void fetchUserInfoAndSetupSpinner(Spinner benefitSpinner, ArrayAdapter<CharSequence> benefitAdapter) {
        HttpUtils.sendGetRequestWithAuth("/getUserInfo", "Bearer " + token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");

                            // 사용자 데이터와 혜택 매핑
                            String userBenefit = mapUserBenefit(userData);

                            // Spinner 기본값 설정
                            int position = benefitAdapter.getPosition(userBenefit);
                            if (position >= 0) {
                                benefitSpinner.setSelection(position);
                            }
                        } else {
                            Log.e("FetchUserInfo", "데이터 가져오기 실패: " + responseData.optString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e("FetchUserInfo", "JSON 파싱 오류: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FetchUserInfo", "서버 요청 실패: " + errorMessage);
            }
        });
    }
    private String mapUserBenefit(JSONObject userData) {
        Map<String, String> dbToSpinnerMapping = new HashMap<>();
        dbToSpinnerMapping.put("disabled_human", "장애인");
        dbToSpinnerMapping.put("multiple_child", "다자녀");
        dbToSpinnerMapping.put("electric_car", "저공해차량");
        dbToSpinnerMapping.put("person_merit", "국가유공자");
        dbToSpinnerMapping.put("tax_payment", "모범납세자");
        dbToSpinnerMapping.put("alone_family", "한부모가정");

        for (String key : dbToSpinnerMapping.keySet()) {
            if (userData.optString(key, "N").equals("Y")) {
                return dbToSpinnerMapping.get(key);
            }
        }
        return "선택사항 없음";
    }

    private void initializeRecyclerView(View view) {
        RecyclerView parkingRecyclerView = view.findViewById(R.id.parkingRecyclerView);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            String parkData = getArguments().getString(ARG_PARK_DATA);
            parseParkData(parkData);
        }

        ParkingAdapter adapter = new ParkingAdapter(parkingList, (name, lat, lot) -> {
            Intent intent = new Intent(requireContext(), DriveActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lot", lot);
            startActivity(intent);
        });
        parkingRecyclerView.setAdapter(adapter);
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

        // 주차 요금 갱신
        updateParkingFees();
    }

    // 주차 요금 갱신 메서드
    private void updateParkingFees() {
        // 주차 요금 계산 후 리스트 갱신
        for (Parking parking : parkingList) {
            double bscPrkCrg = Double.parseDouble(parking.getBaseFee());
            double addPrkCrg = Double.parseDouble(parking.getAddFee());
            double dayMaxCrg = Double.parseDouble(parking.getDayMaxFee());

            // Utils.calculateParkingFee에 totalMinutes를 전달
            String totalFee = Utils.calculateParkingFee(bscPrkCrg, addPrkCrg, dayMaxCrg, totalMinutes);
            parking.setTotalFee(totalFee); // 요금 업데이트
        }

        // RecyclerView 갱신
        ((ParkingAdapter) ((RecyclerView) getView().findViewById(R.id.parkingRecyclerView)).getAdapter()).notifyDataSetChanged();
    }

    // 서버에서 사용자 데이터를 가져와 Spinner 기본값 설정
    private void fetchUserDataAndSetupSpinner(Spinner benefitSpinner) {
        // 모든 옵션 정의 (전체 리스트)
        List<String> allBenefits = new ArrayList<>();
        allBenefits.add("선택사항 없음");
        allBenefits.add("국가유공자");
        allBenefits.add("장애인");
        allBenefits.add("다자녀");
        allBenefits.add("저공해차량");
        allBenefits.add("모범납세자");
        allBenefits.add("한부모가정");

        // 토큰 가져오기
        String token = requireActivity()
                .getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                .getString("auth_token", "");

        if (token == null || token.isEmpty()) {
            Log.e("ParkListBottomSheetFragment", "토큰이 없습니다. 로그인을 확인하세요.");
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 요청으로 사용자 정보 가져오기
        HttpUtils.sendGetRequestWithAuth("/getUserInfo", "Bearer " + token, new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (responseData.getBoolean("success")) {
                            JSONObject userData = responseData.getJSONObject("data");

                            // 유저 조건에 해당하는 항목 가져오기
                            String userBenefit = getUserBenefit(userData);

                            // 전체 리스트 어댑터 설정
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    allBenefits
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            benefitSpinner.setAdapter(adapter);

                            // 유저 조건에 해당하는 항목 선택
                            int position = allBenefits.indexOf(userBenefit);
                            if (position >= 0) {
                                benefitSpinner.setSelection(position);
                            }

                            // 스피너 항목 선택 리스너 설정
                            benefitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                    String selectedItem = (String) parentView.getItemAtPosition(position);
                                    Log.e("Spinner", "선택된 혜택: " + selectedItem);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // 선택 안된 상태 처리
                                }
                            });
                        } else {
                            String message = responseData.optString("message", "오류 발생");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("FetchUserInfo", "JSON 파싱 오류: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FetchUserInfo", "서버 요청 실패: " + errorMessage);
            }
        });
    }



    // 유저 데이터에서 Y 값인 항목만 추출
    private List<String> extractUserBenefits(JSONObject userData) {
        Map<String, String> dbToSpinnerMapping = new HashMap<>();
        dbToSpinnerMapping.put("disabled_human", "장애인");
        dbToSpinnerMapping.put("multiple_child", "다자녀");
        dbToSpinnerMapping.put("electric_car", "저공해차량");
        dbToSpinnerMapping.put("person_merit", "국가유공자");
        dbToSpinnerMapping.put("tax_payment", "모범납세자");
        dbToSpinnerMapping.put("alone_family", "한부모가정");

        List<String> benefits = new ArrayList<>();
        benefits.add("선택사항 없음"); // 기본값

        for (String key : dbToSpinnerMapping.keySet()) {
            if (userData.optString(key, "N").equals("Y")) {
                benefits.add(dbToSpinnerMapping.get(key));
            }
        }

        return benefits;
    }

    // 유저 데이터에서 Y 값에 해당하는 항목 가져오기
    private String getUserBenefit(JSONObject userData) {
        Map<String, String> dbToSpinnerMapping = new HashMap<>();
        dbToSpinnerMapping.put("disabled_human", "장애인");
        dbToSpinnerMapping.put("multiple_child", "다자녀");
        dbToSpinnerMapping.put("electric_car", "저공해차량");
        dbToSpinnerMapping.put("person_merit", "국가유공자");
        dbToSpinnerMapping.put("tax_payment", "모범납세자");
        dbToSpinnerMapping.put("alone_family", "한부모가정");

        for (String key : dbToSpinnerMapping.keySet()) {
            if (userData.optString(key, "N").equals("Y")) {
                return dbToSpinnerMapping.get(key); // 매핑된 혜택 반환
            }
        }

        // 기본값 반환
        return "선택사항 없음";
    }

    private void initializeTimeSpinners(View view) {
        Spinner hourSpinner = view.findViewById(R.id.hourSpinner);
        Spinner minuteSpinner = view.findViewById(R.id.minuteSpinner);

        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.hour_options, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> minuteAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.minute_options, android.R.layout.simple_spinner_item);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        hourSpinner.setSelection(2); // 기본값: 2시간
        minuteSpinner.setSelection(0); // 기본값: 0분

        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateTotalMinutes(hourSpinner, minuteSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        minuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateTotalMinutes(hourSpinner, minuteSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
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

                String distance = parkObject.optString("distance", "0m").replaceAll("[^\\d]", "");
                distance = distance.isEmpty() ? "0" : distance;
                distance = Integer.parseInt(distance) >= 1000
                        ? String.format("%.1fkm", Integer.parseInt(distance) / 1000.0)
                        : NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(distance)) + "m";

                // 주차 데이터 객체 생성
                Parking parking = new Parking(
                        name, remain, distance, "0", lat, lot, 0, baseFee, addFee, dayMaxFee
                );

                // /predict API 호출
                try {
                    // JSON 객체 생성
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("pklt_cd", name);

                    // 서버에 JSON 데이터 전송
                    HttpUtils.sendJsonToServer(jsonData, "/predict", new HttpUtils.HttpResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject responseData) {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    Log.d("parseParkData", "서버 응답: " + responseData.toString());
                                    String predictedValue = responseData.optString("predicted_value", "0");
                                    parkObject.put("predicted_value", predictedValue); // 응답 데이터를 JSON에 추가

                                    parking.setPredictedValue(predictedValue);
                                    parkingList.add(parking); // Parking 객체를 리스트에 추가

                                    Log.e("ParkingList", "Updated Parking Data: " + parkObject.toString());
                                } catch (JSONException e) {
                                    Toast.makeText(requireContext(), "데이터 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("parseParkData", "JSONException", e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            requireActivity().runOnUiThread(() -> {
                                Log.e("parseParkData", "API 호출 실패: " + errorMessage);
                                Toast.makeText(requireContext(), "API 호출 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "JSON 생성 오류", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}