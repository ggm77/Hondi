package com.seohamin.hondi.domain.ride.entity;

/**
 * DB에는 RECRUITING/CANCELED만 저장됨
 * FULL은 저장되지 않고 Ride.getDisplayStatus()에서 인원수 기준으로 그때그때 계산됨
 */
public enum RideStatus {
    // 모집 중
    RECRUITING,
    // 인원 다 참 (계산值, DB에는 저장 안 됨)
    FULL,
    // 방장이 취소
    CANCELED
}
