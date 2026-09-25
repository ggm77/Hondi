package com.seohamin.hondi.domain.ride.dto;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class RideResponseDto {
    private final Long id;
    private final UserSimpleResponseDto host;
    private final String originName;
    private final BigDecimal originLat;
    private final BigDecimal originLon;
    private final String destName;
    private final BigDecimal destLat;
    private final BigDecimal destLon;
    private final Instant departureAt;
    private final Integer capacity;
    private final Integer currentCount;
    private final RideStatus status;
    private final String memo;
    private final Instant createdAt;

    //조회한 유저와 모집글의 관계
    private final RideMyStatus myStatus;

    //참여 중인 유저들
    private final List<UserSimpleResponseDto> members;

    public RideResponseDto(
            final Ride ride,
            final RideMyStatus myStatus,
            final List<UserSimpleResponseDto> members
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
        this.status = ride.getDisplayStatus();
        this.memo = ride.getMemo();
        this.createdAt = ride.getCreatedAt();
        this.myStatus = myStatus;
        this.members = members;
    }
}
