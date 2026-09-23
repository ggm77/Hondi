package com.seohamin.hondi.domain.ride.dto.list;

import com.seohamin.hondi.domain.ride.dto.RideMyStatus;
import com.seohamin.hondi.domain.ride.entity.GenderPolicy;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.service.matching.RideMatchResult;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 모집글 리스트의 아이템
 * 매칭 검색이 아니면 거리, 점수는 null
 */
@Getter
public class RideListItemResponseDto {
    private final Long id;
    private final UserSimpleResponseDto host;
    private final String originName;
    private final BigDecimal originLat;
    private final BigDecimal originLon;
    private final String destName;
    private final BigDecimal destLat;
    private final BigDecimal destLon;
    private final LocalDateTime departureAt;
    private final Integer capacity;
    private final Integer currentCount;
    private final GenderPolicy genderPolicy;
    private final RideStatus status;
    private final RideMyStatus myStatus;

    private final Double originDistanceKm;
    private final Double destDistanceKm;
    private final Long timeDiffMinutes;
    private final Double score;

    public RideListItemResponseDto(final Ride ride, final RideMyStatus myStatus) {
        this(ride, myStatus, null);
    }

    public RideListItemResponseDto(final RideMatchResult result) {
        this(result.ride(), RideMyStatus.NONE, result);
    }

    private RideListItemResponseDto(
            final Ride ride,
            final RideMyStatus myStatus,
            final RideMatchResult result
    ) {
        this.id = ride.getId();
        this.host = new UserSimpleResponseDto(ride.getHost());
        this.originName = ride.getOriginName();
        this.originLat = ride.getOriginLat();
        this.originLon = ride.getOriginLon();
        this.destName = ride.getDestName();
        this.destLat = ride.getDestLat();
        this.destLon = ride.getDestLon();
        this.departureAt = ride.getDepartureAt();
        this.capacity = ride.getCapacity();
        this.currentCount = ride.getCurrentCount();
        this.genderPolicy = ride.getGenderPolicy();
        this.status = ride.getStatus();
        this.myStatus = myStatus;

        if(result != null){
            this.originDistanceKm = round(result.originDistanceKm());
            this.destDistanceKm = round(result.destDistanceKm());
            this.timeDiffMinutes = result.timeDiffMinutes();
            this.score = round(result.score());
        } else {
            this.originDistanceKm = null;
            this.destDistanceKm = null;
            this.timeDiffMinutes = null;
            this.score = null;
        }
    }

    //소수점 둘째 자리까지
    private static double round(final double value) {
        return Math.round(value * 100) / 100.0;
    }
}
