package com.seohamin.hondi.domain.ride.service;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 위치가 서비스 지역(제주도 및 부속 섬)인지 검사하는 클래스
 */
@Component
public class ServiceAreaValidator {

    //제주도, 우도, 추자도, 마라도를 포함하는 대략적인 범위
    private static final BigDecimal MIN_LAT = new BigDecimal("33.0");
    private static final BigDecimal MAX_LAT = new BigDecimal("34.1");
    private static final BigDecimal MIN_LON = new BigDecimal("126.0");
    private static final BigDecimal MAX_LON = new BigDecimal("127.1");

    /**
     * 위도 경도가 서비스 지역 안에 있음을 보장하는 메서드
     * @param lat 위도
     * @param lon 경도
     */
    public void assertInServiceArea(final BigDecimal lat, final BigDecimal lon) {
        if(lat == null || lon == null){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        if(lat.compareTo(MIN_LAT) < 0 || lat.compareTo(MAX_LAT) > 0
                || lon.compareTo(MIN_LON) < 0 || lon.compareTo(MAX_LON) > 0){
            throw new CustomException(ExceptionCode.OUT_OF_SERVICE_AREA);
        }
    }
}
