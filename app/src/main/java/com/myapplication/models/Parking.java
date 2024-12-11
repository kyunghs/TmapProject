package com.myapplication.models;

public class Parking {
    private String name;
    private String remain;
    private String distance;
    private String price; // 기본 가격
    private String lat;
    private String lot;
    private int time; // 주차 시간 (분 단위)
    private String totalFee; // 계산된 총 요금

    // 추가된 필드: 기본 요금, 추가 요금, 일 최대 요금
    private String baseFee;
    private String addFee;
    private String dayMaxFee;
    private String predictedValue;

    public Parking(String name, String remain, String distance, String price, String lat, String lot, int time, String baseFee, String addFee, String dayMaxFee) {
        this.name = name;
        this.remain = remain;
        this.distance = distance;
        this.price = price;
        this.lat = lat;
        this.lot = lot;
        this.time = time;
        this.totalFee = price; // 기본 요금은 초기화된 price로 설정
        this.baseFee = baseFee;
        this.addFee = addFee;
        this.dayMaxFee = dayMaxFee;
        this.predictedValue = predictedValue;
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

    public String getTotalFee() {
        return totalFee;
    }

    // 추가된 Getter 메서드
    public String getBaseFee() {
        return baseFee;
    }

    public String getAddFee() {
        return addFee;
    }

    public String getDayMaxFee() {
        return dayMaxFee;
    }

    public String getPredictedValue() {
        return predictedValue;
    }

    // Setter 메서드 (totalFee 업데이트용)
    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }

    public void setPredictedValue(String predictedValue) {
        this.predictedValue = predictedValue;
    }

    // 거리 값을 숫자로 변환 (단위: 미터)
    public int getDistanceAsNumber() {
        if (distance == null || distance.trim().isEmpty()) {
            return Integer.MAX_VALUE; // 거리 값이 없으면 최대값으로 설정
        }

        if (distance.contains("km")) {
            // "1.2km" -> 1200 (미터로 변환)
            return (int) (Double.parseDouble(distance.replace("km", "").trim()) * 1000);
        } else if (distance.contains("m")) {
            // "500m" -> 500
            return Integer.parseInt(distance.replace("m", "").trim());
        } else {
            return Integer.MAX_VALUE; // 예상치 못한 형식 처리
        }
    }

    // 가격 값을 숫자로 변환
    public int getFeeAsNumber() {
        if (price == null || price.trim().isEmpty()) {
            return Integer.MAX_VALUE; // 가격이 없으면 최대값으로 설정
        }

        try {
            return Integer.parseInt(price.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Integer.MAX_VALUE; // 형식 오류가 있을 경우 최대값
        }
    }
}