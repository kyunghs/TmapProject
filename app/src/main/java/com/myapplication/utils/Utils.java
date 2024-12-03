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
}
