package com.myapplication.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myapplication.R;
import com.myapplication.adapters.PlaceAdapter;
import com.myapplication.models.Place;
import com.myapplication.utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlacesBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_POI_DATA = "poi_data";
    private List<Place> placeList = new ArrayList<>();

    public static PlacesBottomSheetFragment newInstance(String poiData) {
        PlacesBottomSheetFragment fragment = new PlacesBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POI_DATA, poiData);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.places_list, container, false);

        // RecyclerView 초기화
        RecyclerView placesRecyclerView = view.findViewById(R.id.placesRecyclerView);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 어댑터 설정
        PlaceAdapter adapter = new PlaceAdapter(placeList, this::showPopup);
        placesRecyclerView.setAdapter(adapter);

        // 데이터 파싱 및 업데이트
        if (getArguments() != null) {
            String poiData = getArguments().getString(ARG_POI_DATA);
            parsePoiData(poiData);
            adapter.notifyDataSetChanged(); // 데이터 변경 후 어댑터 갱신
        }

        return view;
    }

    private void parsePoiData(String poiData) {
        try {
            JSONArray poiArray = new JSONArray(poiData);
            for (int i = 0; i < poiArray.length(); i++) {
                JSONObject poi = poiArray.getJSONObject(i);
                String name = poi.optString("name", "Unknown Place");
                String address = poi.optString("address", "Unknown Address");
                String latitude = poi.optString("frontLat");
                String longitude = poi.optString("frontLon");

                // Place 객체로 변환하여 리스트에 추가
                placeList.add(new Place(name, address, latitude, longitude));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 팝업을 표시하는 메서드
    private void showPopup(Place place) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.park_popup);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // "예" 버튼 클릭 이벤트
        dialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            try {
                // 선택한 장소의 위도와 경도 가져오기
                String latitude = place.getLatitude();
                String longitude = place.getLongitude();

                // JSON 객체 생성
                JSONObject jsonData = new JSONObject();
                jsonData.put("lat", latitude);
                jsonData.put("lot", longitude);

                // 서버에 JSON 데이터 전송
                HttpUtils.sendJsonToServer(jsonData, "/get/park/info", new HttpUtils.HttpResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseData) {
                        // ParkListBottomSheetFragment 호출 및 데이터 전달
                        ParkListBottomSheetFragment parkListBottomSheet = ParkListBottomSheetFragment.newInstance(responseData.toString());
                        parkListBottomSheet.show(getParentFragmentManager(), "ParkListBottomSheetFragment");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // 에러 처리
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "서버 요청 실패: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                });

                dialog.dismiss(); // 다이얼로그 닫기
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "JSON 생성 오류", Toast.LENGTH_SHORT).show();
            }
        });

        // "아니요" 버튼 클릭 이벤트
        dialog.findViewById(R.id.noBtn).setOnClickListener(v -> {
            PathSelectBottomSheetFragment pathSelectBottomSheetFragment = new PathSelectBottomSheetFragment();
            pathSelectBottomSheetFragment.show(getParentFragmentManager(), "PathSelectBottomSheetFragment");
            dialog.dismiss(); // 현재 다이얼로그 닫기
        });

        dialog.show();
    }
}
