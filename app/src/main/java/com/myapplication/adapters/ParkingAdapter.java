package com.myapplication.adapters;

import android.util.Log;
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

    public interface OnParkingClickListener {
        void onParkingClick(String name, String lat, String lot);
    }

    public ParkingAdapter(List<Parking> parkingList, OnParkingClickListener listener) {
        this.parkingList = parkingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        Parking parking = parkingList.get(position);
        holder.nameTextView.setText(parking.getName());
        holder.remainTextView.setText("남은 자리 : " + parking.getRemain()+"석");
        holder.distanceTextView.setText(parking.getDistance());
        holder.priceTextView.setText("예상 요금 : " + Utils.NumberFormat(parking.getPrice()) + "원");

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

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView remainTextView;
        TextView distanceTextView;
        TextView priceTextView;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.parkingName);
            distanceTextView = itemView.findViewById(R.id.parkingDistance);
            priceTextView = itemView.findViewById(R.id.parkingPrice);
            remainTextView = itemView.findViewById(R.id.remain);
        }
    }
}
