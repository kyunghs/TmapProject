package com.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.R;
import com.myapplication.models.Parking;

import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<Parking> parkingList;

    public ParkingAdapter(List<Parking> parkingList) {
        this.parkingList = parkingList;
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
        holder.distanceTextView.setText(parking.getDistance());
        holder.priceTextView.setText(parking.getPrice());
        holder.availabilityTextView.setText(parking.getAvailability());
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView distanceTextView;
        TextView priceTextView;
        TextView availabilityTextView;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.parkingName);
            distanceTextView = itemView.findViewById(R.id.parkingDistance);
            priceTextView = itemView.findViewById(R.id.parkingPrice);
            availabilityTextView = itemView.findViewById(R.id.parkingAvailability);
        }
    }
}
