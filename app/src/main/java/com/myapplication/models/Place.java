package com.myapplication.models;

public class Place {
    private final String name;
    private final String address;
    private final String latitude;
    private final String longitude;
    private final String distance;

    public Place(String name, String address, String latitude, String longitude, String distance) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }
}
