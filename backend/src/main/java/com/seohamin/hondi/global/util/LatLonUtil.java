package com.seohamin.hondi.global.util;

import java.math.BigDecimal;

public final class LatLonUtil {

    private static final double EARTH_RADIUS = 6371.0; // km

    //위도 1도당 km
    public static final double KM_PER_DEGREE_LAT = 111.32;

    // 인스턴스화 방지
    private LatLonUtil() {}

    /**
     * 두 위도 경도 지점에 대해 km 거리를 계산하는 메서드
     * null 값 들어올 시 -1.0을 리턴함
     * @param fromLat 첫번째 위도
     * @param fromLon 첫번째 경도
     * @param toLat 두번째 위도
     * @param toLon 두번째 경도
     * @return km 거리
     */
    public static double latLonDistance(
            final BigDecimal fromLat,
            final BigDecimal fromLon,
            final BigDecimal toLat,
            final BigDecimal toLon
    ) {

        // NPE 방지
        if (fromLat == null || fromLon == null || toLat == null || toLon == null) {
            return -1.0;
        }

        final double deltaLat = Math.toRadians(toLat.subtract(fromLat).doubleValue());
        final double deltaLon = Math.toRadians(toLon.subtract(fromLon).doubleValue());

        //하버사인 공식 이용
        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(Math.toRadians(fromLat.doubleValue())) * Math.cos(Math.toRadians(toLat.doubleValue())) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 특정 위도에서 거리(km)만큼의 경도 차이를 구하는 메서드
     * 바운딩 박스 계산용
     * @param lat 기준 위도
     * @param distanceKm 거리 (km)
     * @return 경도 차이
     */
    public static double lonDiff(final BigDecimal lat, final double distanceKm) {
        return distanceKm / (KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(lat.doubleValue())));
    }

    /**
     * 거리(km)만큼의 위도 차이를 구하는 메서드
     * 바운딩 박스 계산용
     * @param distanceKm 거리 (km)
     * @return 위도 차이
     */
    public static double latDiff(final double distanceKm) {
        return distanceKm / KM_PER_DEGREE_LAT;
    }
}
