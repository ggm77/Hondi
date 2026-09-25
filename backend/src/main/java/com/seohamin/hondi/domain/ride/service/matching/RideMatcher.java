package com.seohamin.hondi.domain.ride.service.matching;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.global.util.LatLonUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 출발지, 도착지, 출발 시간이 비슷한 모집글을 골라 출발 시간이 가까운 순으로 정렬하는 클래스
 */
@Component
public class RideMatcher {

    //출발지 최대 거리 (km)
    public static final double ORIGIN_RADIUS_KM = 3.0;

    //도착지 최대 거리 (km)
    public static final double DEST_RADIUS_KM = 5.0;

    //출발 시간 최대 차이 (분)
    public static final long TIME_WINDOW_MINUTES = 60;

    /**
     * 후보 모집글 중에서 조건에 맞는 글만 골라 출발 시간이 가까운 순으로 정렬하는 메서드
     * @param candidates 후보 모집글 (DB에서 바운딩 박스, 시간 범위로 1차 필터링 된 것)
     * @param requester 검색하는 유저
     * @param originLat 출발지 위도
     * @param originLon 출발지 경도
     * @param destLat 도착지 위도
     * @param destLon 도착지 경도
     * @param departureAt 희망 출발 시간
     * @return 출발 시간이 가까운 순으로 정렬된 매칭 결과
     */
    public List<RideMatchResult> match(
            final List<Ride> candidates,
            final User requester,
            final BigDecimal originLat,
            final BigDecimal originLon,
            final BigDecimal destLat,
            final BigDecimal destLon,
            final Instant departureAt
    ) {
        return candidates.stream()
                .filter(ride -> canJoin(ride, requester))
                .map(ride -> toMatchResult(ride, originLat, originLon, destLat, destLon, departureAt))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingLong(RideMatchResult::timeDiffMinutes))
                .toList();
    }

    /**
     * 모집글 하나가 거리, 시간 조건에 맞는지 확인하고 매칭 결과를 만드는 메서드
     * 조건을 벗어나면 빈 값 리턴
     */
    public Optional<RideMatchResult> toMatchResult(
            final Ride ride,
            final BigDecimal originLat,
            final BigDecimal originLon,
            final BigDecimal destLat,
            final BigDecimal destLon,
            final Instant departureAt
    ) {
        final double originKm = LatLonUtil.latLonDistance(originLat, originLon, ride.getOriginLat(), ride.getOriginLon());
        if(originKm > ORIGIN_RADIUS_KM){
            return Optional.empty();
        }

        final double destKm = LatLonUtil.latLonDistance(destLat, destLon, ride.getDestLat(), ride.getDestLon());
        if(destKm > DEST_RADIUS_KM){
            return Optional.empty();
        }

        final long timeDiff = Math.abs(Duration.between(departureAt, ride.getDepartureAt()).toMinutes());
        if(timeDiff > TIME_WINDOW_MINUTES){
            return Optional.empty();
        }

        return Optional.of(new RideMatchResult(ride, originKm, destKm, timeDiff));
    }

    /**
     * 유저가 모집글에 참여 가능한지 확인하는 메서드
     * 자기 글에는 참여 불가
     */
    public boolean canJoin(final Ride ride, final User requester) {
        return !ride.getHost().getId().equals(requester.getId());
    }
}
