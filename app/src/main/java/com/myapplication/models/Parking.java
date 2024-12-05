package com.myapplication.models;

public class Parking {
    private String name;
    private String distance;
    private String price;
    private String availability;
    private double lat;
    private double lot;

    public Parking(String name, String distance, String price, String availability, double lat, double lot) {
        this.name = name;
        this.distance = distance;
        this.price = price;
        this.availability = availability;
        this.lat = lat;
        this.lot = lot;
    }

    // Getter 메서드
    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getPrice() {
        return price;
    }

    public String getAvailability() {
        return availability;
    }

    public double getLat() {
        return lat;
    }

    public double getLot() {
        return lot;
    }
}
