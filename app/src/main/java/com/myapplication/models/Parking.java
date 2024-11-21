package com.myapplication.models;

public class Parking {
    private String name;
    private String distance;
    private String price;
    private String availability;

    public Parking(String name, String distance, String price, String availability) {
        this.name = name;
        this.distance = distance;
        this.price = price;
        this.availability = availability;
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
}
