package com.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.myapplication.fragments.PathSelectBottomSheetFragment;
import com.myapplication.utils.HttpSearchUtils;
import com.skt.tmap.engine.navigation.SDKManager;
import com.skt.tmap.engine.navigation.network.ndds.CarOilType;
import com.skt.tmap.engine.navigation.network.ndds.TollCarType;
import com.skt.tmap.engine.navigation.route.RoutePlanType;
import com.skt.tmap.engine.navigation.route.data.MapPoint;
import com.skt.tmap.engine.navigation.route.data.WayPoint;
import com.skt.tmap.vsm.coordinates.VSMCoordinates;
import com.skt.tmap.vsm.data.VSMMapPoint;
import com.skt.tmap.vsm.map.marker.MarkerImage;
import com.skt.tmap.vsm.map.marker.VSMMarkerManager;
import com.skt.tmap.vsm.map.marker.VSMMarkerPoint;
import com.tmapmobility.tmap.tmapsdk.ui.data.CarOption;
import com.tmapmobility.tmap.tmapsdk.ui.data.MapSetting;
import com.tmapmobility.tmap.tmapsdk.ui.fragment.NavigationFragment;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;
import com.tmapmobility.tmap.tmapsdk.ui.view.MapConstant;

import java.util.ArrayList;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "Develop";

    boolean isEDC; // edc 수신 여부

    private NavigationFragment navigationFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private String destinationName = "";
    private String latitude = "";
    private String longitude = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        Intent intent = getIntent();
        if (intent != null) {
            destinationName = intent.getStringExtra("name");
            latitude = intent.getStringExtra("lat");
            longitude = intent.getStringExtra("lot");
        }
        checkPermission();

        /*PathSelectBottomSheetFragment pathSelectBottomSheetFragment = new PathSelectBottomSheetFragment();
        pathSelectBottomSheetFragment.show(getSupportFragmentManager(), "PathSelectBottomSheetFragment");*/
        /*new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Test(destinationName, latitude, longitude);
        }, 1000);*/



        Button test2 = findViewById(R.id.test2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String markerID = "TEST";
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking);
                VSMMarkerPoint marker = new VSMMarkerPoint(markerID);

                marker.setIcon(MarkerImage.fromBitmap(icon));
                marker.setShowPriority(MapConstant.MarkerRenderingPriority.DEFAULT_PRIORITY);
                marker.setText("TEST");

                //현재 위치 보다 조금 옆에 마커를 찍는다.
                VSMMapPoint position = new VSMMapPoint(127.0456768, 37.6523704);
                marker.setPosition(position);

                VSMMarkerManager markerManager = navigationFragment.getMapView().getMarkerManager();
                if (markerManager == null) {
                    Log.e(TAG, "마커 매니저 NULL");
                    return;
                }
                markerManager.addMarker(marker);
            }
        });

        Button test3 = findViewById(R.id.test3);
        test3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationFragment.stopDrive();
            }
        });

        Button test4 = findViewById(R.id.test4);
        test4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationFragment.stopDrive();
            }
        });

    }



    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initUI();
        } else {
            String[] permissionArr = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, 100);
        }
    }

    private void initUI() {
        MapSetting d = new MapSetting();
        d.setShowClosedPopup(false);
        fragmentManager = getSupportFragmentManager();
        navigationFragment = TmapUISDK.Companion.getFragment();

        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.tmapUILayout, navigationFragment);
        transaction.commitAllowingStateLoss();

        isEDC = false;

        //네비게이션 상태 변경 시 callback
        navigationFragment.setDrivingStatusCallback(new TmapUISDK.DrivingStatusCallback() {

            @Override
            public void onStartNavigationInfo(int totalDistanceInMeter, int totalTimeInSec, int tollFree) {
                //경로 시작 정보
            }

            @Override
            public void onUserRerouteComplete() {
                // 사용자 재탐색 동작 완료 시 호출
                Log.e(TAG, "onUserRerouteComplete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onUserRerouteComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onStopNavigation() {
                // 네비게이션 종료 시 호출
                Log.e(TAG, "onStopNavigation");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onStopNavigation", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onStartNavigation() {

                // 네비게이션 시작 시 호출
                Log.e(TAG, "onStartNavigation");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onStartNavigation", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onRouteChanged(int i) {
                // 경로 변경 완료 시 호출
                Log.e(TAG, "onRouteChanged " + i);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onRouteChanged", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPermissionDenied(int i, @Nullable String s) {
                // 권한 에러 발생 시 호출
                Log.e(TAG, "onPermissionDenied " + i + "::" + s);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPermissionDenied", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPeriodicRerouteComplete() {
                // 정주기 재탐색 동작 완료 시 호출
                Log.e(TAG, "onPeriodicRerouteComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPeriodicRerouteComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPeriodicReroute() {
                // 정주기 재탐색 발생 시점에 호출
                Log.e(TAG, "onPeriodicReroute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPeriodicReroute", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedViaPoint() {
                // 경유지 통과 시 호출
                Log.e(TAG, "onPassedViaPoint");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPassedViaPoint", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedTollgate(int i) {
                // 톨게이트 통과 시 호출
                // i 요금

                Log.e(TAG, "onPassedTollgate " + i);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPassedTollgate", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedAlternativeRouteJunction() {
                // 대안 경로 통과 시 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onPassedAlternativeRouteJunction", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onNoLocationSignal(boolean b) {
                // GPS 상태 변화 시점에 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction :: " + b);

                runOnUiThread(() -> Toast.makeText(DriveActivity.this, "onNoLocationSignal " + b, Toast.LENGTH_SHORT).show());


            }

            @Override
            public void onLocationChanged() {
                //위치 갱신 때 마다 호출
                //Log.e(TAG, "onLocationChanged");
            }

            @Override
            public void onFailRouteRequest(@NonNull String errorCode, @NonNull String errorMsg) {
                //경로 탐색 실패 시 호출
                Log.e(TAG, "onFailRouteRequest " + errorCode + "::" + errorMsg);

                runOnUiThread(() -> Toast.makeText(DriveActivity.this, "onFailRouteRequest", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onDoNotRerouteToDestinationComplete() {
                //미리 종료 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDoNotRerouteToDestinationComplete");
                runOnUiThread(() -> Toast.makeText(DriveActivity.this, "onDoNotRerouteToDestinationComplete", Toast.LENGTH_SHORT).show());


            }

            @Override
            public void onDestinationDirResearchComplete() {
                // 건너편 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDestinationDirResearchComplete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onDestinationDirResearchComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onChangeRouteOptionComplete(@NonNull RoutePlanType routePlanType) {
                // 경로 옵션 변경 완료 시 호출
                Log.e(TAG, "onChangeRouteOptionComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onChangeRouteOptionComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onBreakawayFromRouteEvent() {
                // 경로 이탈 재탐색 발생 시점에 호출
                Log.e(TAG, "onBreakawayFromRouteEvent");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onBreakawayFromRouteEvent", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onBreakAwayRequestComplete() {
                //경로 이탈 재탐색 동작 완료 시점에 호출
                Log.e(TAG, "onBreakAwayRequestComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onBreakAwayRequestComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onArrivedDestination(@NonNull String dest, int drivingTime, int drivingDistance) {
                // 목적지 도착 시 호출
                // dest 목적지 명
                // drivingTime 운전시간
                // drivingDistance 운전거리

                Log.e(TAG, "onArrivedDestination");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onArrivedDestination", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onApproachingViaPoint() {
                // 경유지 접근 시점에 호출 (1km 이내)
                Log.e(TAG, "onApproachingViaPoint");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onApproachingViaPoint", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onApproachingAlternativeRoute() {
                // 대안 경로 접근 시 호출
                Log.e(TAG, "onApproachingAlternativeRoute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onApproachingAlternativeRoute", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onForceReroute(@NonNull com.skt.tmap.engine.navigation.network.ndds.NddsDataType.DestSearchFlag destSearchFlag) {
                // 경로 재탐색 발생 시점에 호출
                Log.e(TAG, "onForceReroute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriveActivity.this, "onForceReroute", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }

    private void showDialogContinueRoute(String dest) {
        String message = dest + "(으)로 경로 안내를 이어서 안내 받으시겠습니까?";
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationFragment.continueDrive(true, new TmapUISDK.RouteRequestListener() {
                            @Override
                            public void onSuccess() {
                                Log.e("DriveActivity", "경로 계속 운행 성공");
                            }

                            @Override
                            public void onFail(int i, @Nullable String s) {
                                Log.e("DriveActivity", "경로 계속 운행 실패 " + s);
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
    }

    private void Test(String destinationName, String lat, String lot){

        // 자동차 옵션 설정
        CarOption carOption = new CarOption();
        carOption.setHipassOn(true);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());
        Toast.makeText(DriveActivity.this, lat + lot, Toast.LENGTH_SHORT).show();

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        // `lat`과 `lot`을 double로 변환
        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lot);

        // 목적지
        WayPoint endPoint = new WayPoint(destinationName, new MapPoint(longitude, latitude));

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        Log.e("@!##!", String.valueOf(RoutePlanType.getRoutePlanType(1).getRouteOption()));
        Log.e("@!##!", String.valueOf(RoutePlanType.getRoutePlanType(2).ordinal()));
        Log.e("@!##!", String.valueOf(RoutePlanType.getRoutePlanType(2)));

        navigationFragment.requestRoute(startPoint, null, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(DriveActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);

    }


}
