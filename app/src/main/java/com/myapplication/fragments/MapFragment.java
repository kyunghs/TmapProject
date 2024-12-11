package com.myapplication.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.IdPwChActivity;
import com.myapplication.R;
import com.myapplication.TestWayPoint;
import android.view.inputmethod.EditorInfo;
import com.myapplication.utils.HttpSearchUtils;
import com.myapplication.utils.HttpUtils;
import com.skt.tmap.engine.navigation.SDKManager;
import com.skt.tmap.engine.navigation.livedata.ObservableRouteProgressData;
import com.skt.tmap.engine.navigation.network.ndds.CarOilType;
import com.skt.tmap.engine.navigation.network.ndds.TollCarType;
import com.skt.tmap.engine.navigation.network.ndds.dto.request.TruckType;
import com.skt.tmap.engine.navigation.route.RoutePlanType;
import com.skt.tmap.engine.navigation.route.data.MapPoint;
import com.skt.tmap.engine.navigation.route.data.WayPoint;
import com.skt.tmap.vsm.coordinates.VSMCoordinates;
import com.skt.tmap.vsm.data.VSMMapPoint;
import com.skt.tmap.vsm.map.MapEngine;
import com.skt.tmap.vsm.map.marker.MarkerImage;
import com.skt.tmap.vsm.map.marker.VSMMarkerBase;
import com.skt.tmap.vsm.map.marker.VSMMarkerManager;
import com.skt.tmap.vsm.map.marker.VSMMarkerPoint;
import com.tmapmobility.tmap.tmapsdk.ui.data.CarOption;
import com.tmapmobility.tmap.tmapsdk.ui.data.MapSetting;
import com.tmapmobility.tmap.tmapsdk.ui.data.ObservableRouteData;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteDataCoord;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteDataTraffic;
import com.tmapmobility.tmap.tmapsdk.ui.data.TruckInfoKey;
import com.tmapmobility.tmap.tmapsdk.ui.fragment.NavigationFragment;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;
import com.tmapmobility.tmap.tmapsdk.ui.view.MapConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String TAG = "Develop";

    private boolean isDrivingModeOn = false;
    private NavigationFragment navigationFragment;
    private FragmentTransaction transaction;

    boolean isEDC; // edc 수신 여부
    boolean isRoute; // route data 수신 여부;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // 검색 EditText 초기화
        EditText searchMap = view.findViewById(R.id.searchMap);

        // "안심주행 시작/종료" 버튼
        Button testbtn = view.findViewById(R.id.testbtn);

        // "안심주행 시작/종료" 버튼 클릭 이벤트 설정
        testbtn.setOnClickListener(v -> {
            isDrivingModeOn = !isDrivingModeOn;
            if (isDrivingModeOn) {
                if (navigationFragment != null) {
                    navigationFragment.startSafeDrive();
                }
                testbtn.setText("안심주행 종료"); // 토글 ON
                Log.d(TAG, "Safe Drive Mode ON.");
            } else {
                if (navigationFragment != null) {
                    navigationFragment.stopDrive();
                }
                testbtn.setText("안심주행 시작"); // 토글 OFF
                Log.d(TAG, "Safe Drive Mode OFF.");
            }
        });

        //목적지 검색 시
        searchMap.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String inputText = searchMap.getText().toString();
                HttpSearchUtils.performSearch(inputText, requireContext(), getParentFragmentManager());
                return true;
            }
            return false;
        });

        // 다른 초기화 코드
        checkPermission();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            selectPark();
        }, 1000);
        return view;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initUI(getView());
        } else {
            String[] permissionArr = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, 100);
        }
    }

    private void initUI(View view) {
        MapSetting d = new MapSetting();
        d.setShowClosedPopup(false);

        navigationFragment = TmapUISDK.Companion.getFragment();

        transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.tmapUILayout, navigationFragment);
        transaction.commitAllowingStateLoss();


        isEDC = false;

        // 네비게이션 상태 변경 시 callback
        navigationFragment.setDrivingStatusCallback(new TmapUISDK.DrivingStatusCallback() {

            @Override
            public void onStartNavigationInfo(int totalDistanceInMeter, int totalTimeInSec, int tollFree) {
                // 경로 시작 정보
            }

            @Override
            public void onUserRerouteComplete() {
                // 사용자 재탐색 동작 완료 시 호출
                Log.e(TAG, "onUserRerouteComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onUserRerouteComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onStopNavigation() {
                // 네비게이션 종료 시 호출
                /*buttonLayout.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);*/
                Log.e(TAG, "onStopNavigation");
            }

            @Override
            public void onStartNavigation() {
                // 네비게이션 시작 시 호출
                Log.e(TAG, "onStartNavigation");
            }

            @Override
            public void onRouteChanged(int i) {
                // 경로 변경 완료 시 호출
                Log.e(TAG, "onRouteChanged " + i);
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onRouteChanged", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPermissionDenied(int i, @Nullable String s) {
                // 권한 에러 발생 시 호출
                Log.e(TAG, "onPermissionDenied " + i + "::" + s);
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPermissionDenied", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPeriodicRerouteComplete() {
                // 정주기 재탐색 동작 완료 시 호출
                Log.e(TAG, "onPeriodicRerouteComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPeriodicRerouteComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPeriodicReroute() {
                // 정주기 재탐색 발생 시점에 호출
                Log.e(TAG, "onPeriodicReroute");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPeriodicReroute", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPassedViaPoint() {
                // 경유지 통과 시 호출
                Log.e(TAG, "onPassedViaPoint");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPassedViaPoint", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPassedTollgate(int i) {
                // 톨게이트 통과 시 호출
                // i 요금
                Log.e(TAG, "onPassedTollgate " + i);
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPassedTollgate", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPassedAlternativeRouteJunction() {
                // 대안 경로 통과 시 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onPassedAlternativeRouteJunction", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onNoLocationSignal(boolean b) {
                // GPS 상태 변화 시점에 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction :: " + b);
            }

            @Override
            public void onLocationChanged() {
                // 위치 갱신 때마다 호출
                // Log.e(TAG, "onLocationChanged");
            }

            @Override
            public void onFailRouteRequest(@NonNull String errorCode, @NonNull String errorMsg) {
                // 경로 탐색 실패 시 호출
                Log.e(TAG, "onFailRouteRequest " + errorCode + "::" + errorMsg);
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onFailRouteRequest", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDoNotRerouteToDestinationComplete() {
                // 미리 종료 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDoNotRerouteToDestinationComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onDoNotRerouteToDestinationComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDestinationDirResearchComplete() {
                // 건너편 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDestinationDirResearchComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onDestinationDirResearchComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onChangeRouteOptionComplete(@NonNull RoutePlanType routePlanType) {
                // 경로 옵션 변경 완료 시 호출
                Log.e(TAG, "onChangeRouteOptionComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onChangeRouteOptionComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBreakawayFromRouteEvent() {
                // 경로 이탈 재탐색 발생 시점에 호출
                Log.e(TAG, "onBreakawayFromRouteEvent");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onBreakawayFromRouteEvent", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBreakAwayRequestComplete() {
                // 경로 이탈 재탐색 동작 완료 시점에 호출
                Log.e(TAG, "onBreakAwayRequestComplete");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onBreakAwayRequestComplete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onArrivedDestination(@NonNull String dest, int drivingTime, int drivingDistance) {
                // 목적지 도착 시 호출
                // dest 목적지 명
                // drivingTime 운전시간
                // drivingDistance 운전거리
                Log.e(TAG, "onArrivedDestination");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onArrivedDestination", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onApproachingViaPoint() {
                // 경유지 접근 시점에 호출 (1km 이내)
                Log.e(TAG, "onApproachingViaPoint");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onApproachingViaPoint", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onApproachingAlternativeRoute() {
                // 대안 경로 접근 시 호출
                Log.e(TAG, "onApproachingAlternativeRoute");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onApproachingAlternativeRoute", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onForceReroute(@NonNull com.skt.tmap.engine.navigation.network.ndds.NddsDataType.DestSearchFlag destSearchFlag) {
                // 경로 재탐색 발생 시점에 호출
                Log.e(TAG, "onForceReroute");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onForceReroute", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void selectPark() {
        HttpUtils.sendJsonToServer(null, "/selectPark", new HttpUtils.HttpResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseData) {
                try {
                    // 최상위 JSONObject에서 parkList 배열 가져오기
                    JSONArray locations = responseData.getJSONArray("parkList");
                    Log.e("parkList!!", String.valueOf(locations));
                    List<VSMMapPoint> positions = new ArrayList<>();
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject location = locations.getJSONObject(i);
                        double lat = location.getDouble("lat");
                        double lot = location.getDouble("lot");
                        positions.add(new VSMMapPoint(lot, lat)); // 경도(lot), 위도(lat)
                    }

                    // UI 스레드에서 pin() 호출
                    requireActivity().runOnUiThread(() -> pin(positions, locations));
                } catch (JSONException e) {
                    Log.e("selectPark", "JSON 파싱 실패", e);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "JSON 파싱 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void pin(List<VSMMapPoint> positions, JSONArray locations) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking);

        VSMMarkerManager markerManager = navigationFragment.getMapView().getMarkerManager();
        if (markerManager == null) {
            Log.e("pin", "마커 매니저 NULL");
            return;
        }

        // 각 위치에 대해 마커 생성 및 추가
        for (int i = 0; i < positions.size(); i++) {
            Log.e("!@!@!#!@#", positions.toString());
            String markerID = "Marker" + i; // 마커 ID 생성
            VSMMarkerPoint marker = new VSMMarkerPoint(markerID);

            marker.setIcon(MarkerImage.fromBitmap(icon));
            marker.setShowPriority(MapConstant.MarkerRenderingPriority.DEFAULT_PRIORITY);
            marker.setText(markerID);
            marker.setPosition(positions.get(i)); // 위치 설정

            markerManager.addMarker(marker); // 마커 추가
        }

        Log.d("pin", "모든 마커가 추가되었습니다.");

        navigationFragment.setHitEventListener(new MapEngine.OnHitObjectListener() {
            @Override
            public boolean OnHitObjectPOI(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
                return false;
            }

            @Override
            public boolean OnHitObjectMarker(VSMMarkerBase vsmMarkerBase, Bundle bundle) {
                String markerID = vsmMarkerBase.getId();
                Log.e("Marker Hit", "Marker ID: " + markerID);

                try {
                    // Marker ID에서 인덱스 추출 ("Marker" 뒤의 숫자)
                    int index = Integer.parseInt(markerID.replace("Marker", ""));

                    // 인덱스에 해당하는 주차장 정보 가져오기
                    JSONObject location = locations.getJSONObject(index);
                    String pkltName = location.optString("pklt_nm", "정보 없음");
                    String address = location.optString("addr", "주소 없음");
                    String baseCrg = location.optString("bsc_prk_crg", "기본 요금 정보 없음");
                    String addCrg = location.optString("add_prk_crg", "추가 요금 정보 없음");
                    String dayCrg = location.optString("day_max_crg", "일일 최대 요금 정보 없음");
                    String totalCnt = location.optString("tpkct", "총 주차면수 없음");
                    String nowCnt = location.optString("now_prk_vhcl_cnt", "현재 주차면수 없음");
                    String openTime = location.optString("wd_oper_bgng_tm", "시작 정보 없음");
                    String closeTime = location.optString("wd_oper_end_tm", "종료 정보 없음");

                    // 팝업 띄우기
                    showParkingInfoPopup(pkltName, address, baseCrg, addCrg, dayCrg, totalCnt, nowCnt, openTime, closeTime);
                } catch (JSONException e) {
                    Log.e("OnHitObjectMarker", "JSON 파싱 오류", e);
                }
                return false;
            }

            @Override
            public boolean OnHitObjectOilInfo(String s, int i, VSMMapPoint vsmMapPoint) {
                return false;
            }

            @Override
            public boolean OnHitObjectTraffic(String s, int i, String s1, String s2, String s3, VSMMapPoint vsmMapPoint) {
                return false;
            }

            @Override
            public boolean OnHitObjectCctv(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
                return false;
            }

            @Override
            public boolean OnHitObjectAlternativeRoute(String s, VSMMapPoint vsmMapPoint) {
                return false;
            }

            @Override
            public boolean OnHitObjectRouteFlag(String s, int i, VSMMapPoint vsmMapPoint) {
                return false;
            }

            @Override
            public boolean OnHitObjectRouteLine(String s, int i, VSMMapPoint vsmMapPoint) {
                return false;
            }

            @Override
            public boolean OnHitObjectNone(VSMMapPoint vsmMapPoint) {
                return false;
            }
        }, new MapEngine.OnHitCalloutPopupListener() {
            @Override
            public void OnHitCalloutPopupPOI(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
            }

            @Override
            public void OnHitCalloutPopupMarker(VSMMarkerBase vsmMarkerBase) {
            }

            @Override
            public void OnHitCalloutPopupTraffic(String s, int i, String s1, String s2, String s3, VSMMapPoint vsmMapPoint) {
            }

            @Override
            public void OnHitCalloutPopupCctv(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
            }

            @Override
            public void OnHitCalloutPopupUserDefine(String s, int i, VSMMapPoint vsmMapPoint) {
            }
        });
    }

    private void showParkingInfoPopup(String name, String address, String baseCrg, String addCrg,
                                      String dayCrg, String totalCnt, String nowCnt, String openTime, String closeTime) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.park_info_popup);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // TextView 설정
        TextView nameTextView = dialog.findViewById(R.id.nameTextView);
        TextView addressTextView = dialog.findViewById(R.id.addressTextView);
        TextView baseCrgTextView = dialog.findViewById(R.id.baseCrgTextView);
        TextView addCrgTextView = dialog.findViewById(R.id.addCrgTextView);
        TextView dayCrgTextView = dialog.findViewById(R.id.dayCrgTextView);
        TextView totalCntTextView = dialog.findViewById(R.id.totalCntTextView);
        TextView nowCntTextView = dialog.findViewById(R.id.nowCntTextView);
        TextView openTimeTextView = dialog.findViewById(R.id.openTimeTextView);
        TextView closeTimeTextView = dialog.findViewById(R.id.closeTimeTextView);

        // 데이터를 텍스트 뷰에 바인딩
        nameTextView.setText(name);
        addressTextView.setText(address);
        baseCrgTextView.setText("기본 요금: " + baseCrg + "원");
        addCrgTextView.setText("추가 요금: " + addCrg + "원");
        dayCrgTextView.setText("일일 최대 요금: " + dayCrg + "원");
        totalCntTextView.setText("총 주차면수: " + totalCnt + "석");
        nowCntTextView.setText("현재 남은 자리: " + nowCnt + "석");
        openTimeTextView.setText("운영 시작: " + formatTime(openTime));
        closeTimeTextView.setText("운영 종료: " + formatTime(closeTime));

        dialog.show();
    }
    // 시간 형식을 변환하는 함수
    private String formatTime(String time) {
        if (time.length() == 4) {
            return time.substring(0, 2) + ":" + time.substring(2, 4);
        }
        return time; // 변환 실패 시 원래 값 반환
    }
}
