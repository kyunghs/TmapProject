package com.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.DriveActivity;
import com.myapplication.MainActivity;
import com.myapplication.R;
import com.myapplication.utils.HttpSearchUtils;
import com.myapplication.utils.Utils;
import com.skt.tmap.engine.navigation.SDKManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PathSelectBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_LAT = "lat";
    private static final String ARG_LOT = "lot";

    private String lat;
    private String lot;

    // TextViews for binding
    private TextView firstTime, firstDist, firstFee, firstArrive;
    private TextView secondTime, secondDist, secondFee, secondArrive;
    private TextView thirdTime, thirdDist, thirdFee, thirdArrive;
    private LinearLayout firstRoute, secondRoute, thirdRoute;

    public static PathSelectBottomSheetFragment newInstance(String lat, String lot) {
        PathSelectBottomSheetFragment fragment = new PathSelectBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LAT, lat);
        args.putString(ARG_LOT, lot);
        fragment.setArguments(args);
        return fragment;
    }

    // 콜백 인터페이스 정의
    public interface RouteSelectionListener {
        void onRouteSelected(int route);
    }

    private RouteSelectionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RouteSelectionListener) {
            listener = (RouteSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RouteSelectionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.path_select_sheet, container, false);

        // Bind views
        firstTime = view.findViewById(R.id.firstTime);
        firstDist = view.findViewById(R.id.firstDist);
        firstFee = view.findViewById(R.id.firstFee);
        firstArrive = view.findViewById(R.id.firstArrive);
        firstRoute = view.findViewById(R.id.firstRoute);

        secondTime = view.findViewById(R.id.secondTime);
        secondDist = view.findViewById(R.id.secondDist);
        secondFee = view.findViewById(R.id.secondFee);
        secondArrive = view.findViewById(R.id.secondArrive);
        secondRoute = view.findViewById(R.id.secondRoute);

        thirdTime = view.findViewById(R.id.thirdTime);
        thirdDist = view.findViewById(R.id.thirdDist);
        thirdFee = view.findViewById(R.id.thirdFee);
        thirdArrive = view.findViewById(R.id.thirdArrive);
        thirdRoute = view.findViewById(R.id.thirdRoute);

        // Retrieve arguments
        if (getArguments() != null) {
            lat = getArguments().getString(ARG_LAT);
            lot = getArguments().getString(ARG_LOT);
        }

        // Example coordinates (set actual values here)
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        final double currentLong = currentLocation.getLongitude();
        final double currentLat = currentLocation.getLatitude();
        final double targetLot = Double.parseDouble(lot);
        final double targetLat = Double.parseDouble(lat);

        // Route 버튼 클릭 시 동작 설정
        firstRoute.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteSelected(0); // Route 0 선택
            }
            dismiss(); // Fragment 종료
        });

        secondRoute.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteSelected(1); // Route 1 선택
            }
            dismiss(); // Fragment 종료
        });

        thirdRoute.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteSelected(2); // Route 2 선택
            }
            dismiss(); // Fragment 종료
        });

        // HTTP requests
        for (int i = 0; i < 3; i++) {
            final int finalI = i; // Mark `i` as effectively final
            HttpSearchUtils.performRouteRequest(requireContext(), finalI, currentLong, currentLat, targetLot, targetLat, new HttpSearchUtils.RouteRequestCallback() {
                @Override
                public void onSuccess(JSONObject rootObject) {
                    try {
                        int totalTime = 0;
                        int totalDistance = 0;
                        int totalFare = 0;

                        if (rootObject.has("features")) {
                            JSONArray features = rootObject.getJSONArray("features");
                            if (features.length() > 0) {
                                JSONObject firstFeature = features.getJSONObject(0);
                                if (firstFeature.has("properties")) {
                                    JSONObject properties = firstFeature.getJSONObject("properties");
                                    if (properties.has("totalTime")) {
                                        totalTime = properties.getInt("totalTime");
                                    }
                                    if (properties.has("totalDistance")) {
                                        totalDistance = properties.getInt("totalDistance");
                                    }
                                    if (properties.has("totalFare")) {
                                        totalFare = properties.getInt("totalFare");
                                    }
                                }
                            }
                        }

                        // Update UI on main thread
                        int finalTotalTime = totalTime;
                        int finalTotalDistance = totalDistance;
                        int finalTotalFare = totalFare;

                        // 현재 시간을 기준으로 도착 시간을 계산
                        LocalTime currentTime = LocalTime.now(); // 현재 시간을 가져옴
                        LocalTime arrivalTime = currentTime.plusMinutes(finalTotalTime / 60); // totalTime을 분 단위로 더함

                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.KOREAN); // 오전/오후 포함한 포맷


                        requireActivity().runOnUiThread(() -> {
                            switch (finalI) {
                                case 0:
                                    firstTime.setText(String.valueOf(finalTotalTime / 60));
                                    firstDist.setText(Utils.NumberFormat(String.format("%.1f", finalTotalDistance / 1000.0)) + "km");
                                    firstFee.setText(finalTotalFare + "원");
                                    firstArrive.setText(arrivalTime.format(timeFormatter) + " 도착");
                                    break;
                                case 1:
                                    secondTime.setText(String.valueOf(finalTotalTime / 60));
                                    secondDist.setText(Utils.NumberFormat(String.format("%.1f", finalTotalDistance / 1000.0)) + "km");
                                    secondFee.setText(finalTotalFare + "원");
                                    secondArrive.setText(arrivalTime.format(timeFormatter) + " 도착");
                                    break;
                                case 2:
                                    thirdTime.setText(String.valueOf(finalTotalTime / 60));
                                    thirdDist.setText(Utils.NumberFormat(String.format("%.1f", finalTotalDistance / 1000.0)) + "km");
                                    thirdFee.setText(finalTotalFare + "원");
                                    thirdArrive.setText(arrivalTime.format(timeFormatter) + " 도착");
                                    break;
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "오류: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }

        return view;
    }
}