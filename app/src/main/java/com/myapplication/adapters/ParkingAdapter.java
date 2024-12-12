package com.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.R;
import com.myapplication.models.Parking;
import com.myapplication.utils.Utils;

import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {
    private List<Parking> parkingList;
    private OnParkingClickListener listener;

    private int totalMinutes;
    private String selectedBenefit;

    public interface OnParkingClickListener {
        void onParkingClick(String name, String lat, String lot);
    }

    public ParkingAdapter(List<Parking> parkingList, OnParkingClickListener listener, int totalMinutes, String selectedBenefit) {
        this.parkingList = parkingList;
        this.listener = listener;
        this.totalMinutes = totalMinutes;
        this.selectedBenefit = selectedBenefit;
    }

    private double calculateDiscountedFee(double baseFee, double addFee, double dayMaxFee, int totalMinutes, String benefit) {
        double totalFee = baseFee + (addFee * (totalMinutes / 60.0)); // 기본 요금 + 추가 요금 계산

        // 일일 최대 요금 적용
        if (totalFee > dayMaxFee) {
            totalFee = dayMaxFee;
        }

        switch (benefit) {
            case "장애인":
                if (totalMinutes <= 180) {
                    totalFee = 0; // 최초 3시간 무료
                } else {
                    totalFee *= 0.2; // 80% 감면
                }
                break;
            case "다자녀":
                totalFee *= 0.5;
                break;
            case "저공해차량":
                if (totalMinutes <= 60) {
                    totalFee = 0; // 1시간 무료
                } else {
                    totalFee *= 0.5; // 50% 감면
                }
                break;
            case "국가유공자":
                totalFee *= 0.2;
                break;
            case "모범납세자":
                totalFee = 0;
                break;
            case "한부모가정":
                totalFee *= 0.5;
                break;
            default:
                break;
        }

        return totalFee;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    // ParkingAdapter 클래스의 onBindViewHolder 메서드
    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        Parking parking = parkingList.get(position);

        holder.nameTextView.setText(parking.getName());
        holder.remainTextView.setText("남은 자리 : " + parking.getRemain());
        holder.distanceTextView.setText(parking.getDistance());

        // 할인 조건 적용
        double baseFee = Double.parseDouble(parking.getBaseFee());
        double addFee = Double.parseDouble(parking.getAddFee());
        double dayMaxFee = Double.parseDouble(parking.getDayMaxFee());
        double discountedFee = calculateDiscountedFee(baseFee, addFee, dayMaxFee, totalMinutes, selectedBenefit);
        holder.priceTextView.setText("예상 요금 : " + Utils.NumberFormat(String.valueOf(discountedFee)) + "원");

        holder.predictValue.setText("예상 자리 : " + parking.getPredictedValue());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParkingClick(parking.getName(), parking.getLat(), parking.getLot());
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public void updateParameters(int totalMinutes, String selectedBenefit) {
        this.totalMinutes = totalMinutes;
        this.selectedBenefit = selectedBenefit;
        notifyDataSetChanged(); // 데이터 변경을 알림
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView remainTextView;
        TextView distanceTextView;
        TextView priceTextView;
        TextView predictValue;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.parkingName);
            distanceTextView = itemView.findViewById(R.id.parkingDistance);
            priceTextView = itemView.findViewById(R.id.parkingPrice);
            remainTextView = itemView.findViewById(R.id.remain);
            predictValue = itemView.findViewById(R.id.parkingExpectedAvailability);
        }
    }
}