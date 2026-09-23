package com.seohamin.hondi.domain.ride.service.matching;

import com.seohamin.hondi.domain.ride.entity.Ride;

/**
 * 모집글 매칭 결과
 * @param ride 매칭된 모집글
 * @param originDistanceKm 출발지 사이 거리 (km)
 * @param destDistanceKm 도착지 사이 거리 (km)
 * @param timeDiffMinutes 출발 시간 차이 (분, 절댓값)
 * @param score 0~1 사이 점수, 높을수록 잘 맞음
 */
public record RideMatchResult(
        Ride ride,
        double originDistanceKm,
        double destDistanceKm,
        long timeDiffMinutes,
        double score
) {}
