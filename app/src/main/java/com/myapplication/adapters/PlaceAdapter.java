package com.myapplication.adapters;

// import 구문 추가: 필요한 클래스 import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.R;
import com.myapplication.models.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList;
    private OnPlaceClickListener listener;

    public PlaceAdapter(List<Place> placeList, OnPlaceClickListener listener) {
        this.placeList = placeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.placeName.setText(place.getName());
        holder.addrName.setText(place.getAddress());
        holder.distName.setText(place.getDistance());
        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        TextView addrName;
        TextView distName;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            addrName = itemView.findViewById(R.id.addrName);
            distName = itemView.findViewById(R.id.distName);
        }
    }
}
