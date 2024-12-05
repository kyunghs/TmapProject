package com.myapplication.models;

public class Parking {
    private String name;
    private String remain;
    private String distance;
    private String price;
    private String lat;
    private String lot;

    //목적지까지 걸리는 소요시간 : totalTime = 초단위
    private int time;

    public Parking(String name, String remain, String distance, String price, String lat, String lot, int time) {
        this.name = name;
        this.remain = remain;
        this.distance = distance;
        this.price = price;
        this.lat = lat;
        this.lot = lot;
        this.time = time;
    }

    // Getter 메서드
    public String getName() {
        return name;
    }

    public String getRemain() {
        return remain;
    }

    public String getDistance() {
        return distance;
    }

    public String getPrice() {
        return price;
    }

    public String getLat() {
        return lat;
    }

    public String getLot() {
        return lot;
    }

    public int getTime() {
        return time;
    }
}
