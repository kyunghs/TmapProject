package com.myapplication.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.R;
import com.myapplication.TestWayPoint;
import android.view.inputmethod.EditorInfo;
import com.myapplication.utils.HttpSearchUtils;
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
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation_view); // 하단 내비게이션 뷰
        FrameLayout mapBottomContainer = requireActivity().findViewById(R.id.mapBottomContainer); // map_bottom.xml 및 path_select_sheet.xml을 표시할 컨테이너

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
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onStopNavigation", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onStartNavigation() {
                // 네비게이션 시작 시 호출
                Log.e(TAG, "onStartNavigation");
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onStartNavigation", Toast.LENGTH_SHORT).show());
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
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "onNoLocationSignal " + b, Toast.LENGTH_SHORT).show());
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



    /**
     * 안전운행
     */
    private void clickButton1() {
/*        buttonLayout.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);*/
        navigationFragment.startSafeDrive();
    }

    /**
     * 경로안내
     */
    private void clickButton2() {
/*
        buttonLayout.setVisibility(View.GONE);
*/

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        // 현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // 목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999));

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        navigationFragment.requestRoute(startPoint, null, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    /**
     * 경유지 추가 경로안내
     */
    private void clickButton3() {

        // 자동차 옵션 설정
        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.Gasoline);
        carOption.setHipassOn(true);

        // 현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());
        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // 경유지 설정
        ArrayList<WayPoint> passList = new ArrayList<>();

        WayPoint pass1 = new WayPoint("가", new MapPoint(126.9628, 37.5382));
        passList.add(pass1);

        WayPoint pass2 = new WayPoint("나", new MapPoint(126.9385, 37.5199));
        passList.add(pass2);

        WayPoint pass3 = new WayPoint("다", new MapPoint(126.9620, 37.5223));
        passList.add(pass3);

        // 목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        navigationFragment.requestRoute(startPoint, passList, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
/*
                stopButton.setVisibility(View.VISIBLE);
*/
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    private void clickButton4() {
/*
        buttonLayout.setVisibility(View.GONE);
*/

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        // 트럭 경로 요청하기 위한 추가 정보
        HashMap<String, String> truckDetailInfo = new HashMap<>();
        truckDetailInfo.put(TruckInfoKey.TruckType.getValue(), TruckType.Truck.toString());
        truckDetailInfo.put(TruckInfoKey.TruckWeight.getValue(), "2000.0"); // 단위 kg 화물의 무게
        truckDetailInfo.put(TruckInfoKey.TruckHeight.getValue(), "400.0"); // 단위 cm 화물차 높이

        carOption.setTruckInfo(truckDetailInfo);

        // 현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // 목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Highway);

        // 길안내 바로 시작
        navigationFragment.requestRoute(startPoint, null, endPoint, true, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
/*
                stopButton.setVisibility(View.VISIBLE);
*/
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    /**
     * 경유지 맥스
     */
    private void clickButton5() {
/*
        buttonLayout.setVisibility(View.GONE);
*/

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        // 현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());
        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // 목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        // 길안내 시작
        navigationFragment.requestRoute(startPoint, TestWayPoint.point, endPoint, true, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
/*
                stopButton.setVisibility(View.VISIBLE);
*/
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    private void clickButton6() {
        String markerID = "TEST";
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi);
        VSMMarkerPoint marker = new VSMMarkerPoint(markerID);

        marker.setIcon(MarkerImage.fromBitmap(icon));
        marker.setShowPriority(MapConstant.MarkerRenderingPriority.DEFAULT_PRIORITY);
        marker.setText("TEST");

        // 현재 위치보다 조금 옆에 마커를 찍는다.
        VSMMapPoint position = new VSMMapPoint(SDKManager.getInstance().getCurrentPosition().getLongitude() - 0.0002, SDKManager.getInstance().getCurrentPosition().getLatitude() + 0.0002);
        marker.setPosition(position);

        VSMMarkerManager markerManager = navigationFragment.getMapView().getMarkerManager();
        if (markerManager == null) {
            Log.e(TAG, "마커 매니저 NULL");
            return;
        }

        markerManager.removeMarker(markerID);
        markerManager.addMarker(marker);

        navigationFragment.setHitEventListener(new MapEngine.OnHitObjectListener() {
            @Override
            public boolean OnHitObjectPOI(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
                return false;
            }

            @Override
            public boolean OnHitObjectMarker(VSMMarkerBase vsmMarkerBase, Bundle bundle) {
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

    private void clickButton7() {
        subscribeEDCData();
    }

    private Observer<Bundle> edcListener = new Observer<Bundle>() {
        @Override
        public void onChanged(Bundle bundle) {
            if (bundle != null) {
                Log.e(TAG, bundle.toString());
            }
        }
    };

    private void subscribeEDCData() {
        if (isEDC) {
            isEDC = false;
            TmapUISDK.observableEDCData.removeObserver(edcListener);
/*
            button7.setText("EDC 수신 등록");
*/
        } else {
            isEDC = true;
            TmapUISDK.observableEDCData.observe(this, edcListener);
/*
            button7.setText("EDC 수신 해제");
*/
        }
    }

    private void clickButton8() {
        subscribeRouteData();
    }

    private void clickButtonTest() {
/*
        buttonLayout.setVisibility(View.GONE);
*/

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        // 현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // 목적지
        WayPoint endPoint = new WayPoint("한국공학대학교", new MapPoint(126.73402132406565, 37.3396700356916));

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        navigationFragment.requestRoute(startPoint, null, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
/*
                stopButton.setVisibility(View.VISIBLE);
*/
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    private Observer<ObservableRouteData> routeDataListener = new Observer<ObservableRouteData>() {
        @Override
        public void onChanged(ObservableRouteData data) {
            Log.e(TAG, data.toString());
            int distance = data.getNTotalDist(); // 목적지까지 총 남은거리(m)
            int time = data.getNTotalTime(); // 목적지까지 총 남은 시간(초)
            int toll = data.getTollFare(); // 톨게이트 요금
            int taxi = data.getTaxiFare(); // 택시 요금
            List<RouteDataCoord> coordList = data.getRouteCoordinates(); // 경로 좌표 데이터(위도, 경도)
            List<RouteDataTraffic> trafficInfoList = data.getRouteTrafficInfos(); // 경로 복잡도 정보

            double lat = coordList.get(0).getLatitude();
            double lon = coordList.get(0).getLongitude();

            // 혼잡도 정보의 index , RouteDataCoord List의 index
            int endIndex = trafficInfoList.get(0).getEndIndexInCoordinate();
            int startIndex = trafficInfoList.get(0).getStartIndexInCoordinate();

            ObservableRouteProgressData.TrafficStatus status = trafficInfoList.get(0).getTrafficStatus(); // 혼잡도
        }
    };

    private void subscribeRouteData() {
        if (isRoute) {
            isRoute = false;
            TmapUISDK.observableRouteData.removeObserver(routeDataListener);
/*
            button8.setText("Route Data 수신 등록");
*/
        } else {
            isRoute = true;
            TmapUISDK.observableRouteData.observe(this, routeDataListener);
/*
            button8.setText("Route Data 수신 해제");
*/
        }
    }

    /*private void initUISDK() {
        TmapUISDK.Companion.initialize(getActivity(), CLIENT_ID, API_KEY, USER_KEY, DEVICE_KEY, new TmapUISDK.InitializeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "success initialize");
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(getActivity(), i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }

            @Override
            public void savedRouteInfoExists(@Nullable String dest) {
                Log.e(TAG, "목적지 : " + dest);
                if (dest != null) {
                    showDialogContinueRoute(dest);
                }
            }
        });
    }*/

    /*private void showDialogContinueRoute(String dest) {
        String message = dest + "(으)로 경로 안내를 이어서 안내 받으시겠습니까?";
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationFragment.continueDrive(true, new TmapUISDK.RouteRequestListener() {
                            @Override
                            public void onSuccess() {
                                Log.e("MapFragment", "경로 계속 운행 성공");
*//*                                buttonLayout.setVisibility(View.GONE);
                                stopButton.setVisibility(View.VISIBLE);*//*
                            }

                            @Override
                            public void onFail(int i, @Nullable String s) {
                                Log.e("MapFragment", "경로 계속 운행 실패 " + s);
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationFragment.clearContinueDriveInfo();
                    }
                })
                .show();
    }*/
}