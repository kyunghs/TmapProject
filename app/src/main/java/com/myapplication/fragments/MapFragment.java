package com.myapplication.fragments;

import android.Manifest;
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

                    List<VSMMapPoint> positions = new ArrayList<>();
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject location = locations.getJSONObject(i);
                        double lat = location.getDouble("lat");
                        double lot = location.getDouble("lot");
                        positions.add(new VSMMapPoint(lot, lat)); // 경도(lot), 위도(lat)
                    }

                    // UI 스레드에서 pin() 호출
                    requireActivity().runOnUiThread(() -> pin(positions));
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

    private void pin(List<VSMMapPoint> positions) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking);

        VSMMarkerManager markerManager = navigationFragment.getMapView().getMarkerManager();
        if (markerManager == null) {
            Log.e("pin", "마커 매니저 NULL");
            return;
        }

        // 각 위치에 대해 마커 생성 및 추가
        for (int i = 0; i < positions.size(); i++) {
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
                Toast.makeText(getActivity(), vsmMarkerBase.getId(), Toast.LENGTH_SHORT).show();
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

    private void clickButton6() {


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