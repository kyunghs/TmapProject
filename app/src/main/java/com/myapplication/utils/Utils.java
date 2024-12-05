package com.myapplication.utils;

public class Utils {

    /**
     * 두 위도와 경도를 기반으로 거리 계산 후 결과 반환 (Haversine 공식)
     *
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 두 지점 간의 거리 (String 형식, km 또는 m 단위 포함)
     */
    public static String calculateDistanceAsString(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // 지구 반지름 (km)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c * 1000; // km -> m 변환

        // 거리 문자열 반환
        if (distance >= 1000) {
            // km 단위: 소수점 첫째 자리까지 표시
            return String.format("%.1f km", distance / 1000);
        } else {
            // m 단위: 정수만 표시
            return String.format("%.0f m", distance);
        }
    }

    /**
     * 주차 요금을 계산하는 메소드
     *
     * @param bscPrkCrg 기본 주차 요금
     * @param addPrkCrg 추가 단위 요금
     * @param dayMaxCrg 일일 최대 요금
     * @param parkingMinutes 주차 시간 (분 단위)
     * @return 총 주차 요금
     */
    public static String calculateParkingFee(double bscPrkCrg, double addPrkCrg, double dayMaxCrg, int parkingMinutes) {
        // 기본 시간 (5분)
        int baseTimeMinutes = 5;
        int additionalUnitTimeMinutes = 5;

        // 기본 요금 계산
        double totalFee = bscPrkCrg;

        // 기본 시간 제외한 추가 시간 계산
        int remainingMinutes = Math.max(0, parkingMinutes - baseTimeMinutes);

        // 추가 요금 계산
        int additionalUnits = remainingMinutes / additionalUnitTimeMinutes;
        double additionalFee = additionalUnits * addPrkCrg;

        totalFee += additionalFee;

        // 일일 최대 요금 적용
        int days = parkingMinutes / (24 * 60); // 총 주차 일수
        int remainingMinutesInDay = parkingMinutes % (24 * 60); // 하루로 나눈 나머지 시간
        double dailyMaxFee = days * dayMaxCrg;

        // 남은 시간에 대해 추가 요금 계산
        double remainingFee = calculateSingleDayFee(bscPrkCrg, addPrkCrg, dayMaxCrg, remainingMinutesInDay);

        // 총 요금 계산 (일일 최대 요금 + 남은 시간 요금)
        double finalFee = dailyMaxFee + remainingFee;

        // 최종 요금 반환 (일일 최대 요금을 초과하지 않도록 제한)
        int roundedFee = (int) Math.min(totalFee, finalFee);

        // 문자열로 반환
        return String.valueOf(roundedFee);
    }

    /**
     * 단일 하루 주차 요금을 계산하는 메소드
     *
     * @param bscPrkCrg 기본 주차 요금
     * @param addPrkCrg 추가 단위 요금
     * @param dayMaxCrg 일일 최대 요금
     * @param parkingMinutes 주차 시간 (분 단위)
     * @return 하루의 주차 요금
     */
    private static double calculateSingleDayFee(double bscPrkCrg, double addPrkCrg, double dayMaxCrg, int parkingMinutes) {
        int baseTimeMinutes = 5;
        int additionalUnitTimeMinutes = 5;

        // 기본 요금 계산
        double totalFee = bscPrkCrg;

        // 기본 시간 제외한 추가 시간 계산
        int remainingMinutes = Math.max(0, parkingMinutes - baseTimeMinutes);

        // 추가 요금 계산
        int additionalUnits = remainingMinutes / additionalUnitTimeMinutes;
        double additionalFee = additionalUnits * addPrkCrg;

        totalFee += additionalFee;

        // 하루 최대 요금 제한
        return Math.min(totalFee, dayMaxCrg);
    }
}
