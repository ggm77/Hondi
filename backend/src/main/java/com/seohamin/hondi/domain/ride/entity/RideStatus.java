package com.seohamin.hondi.domain.ride.entity;

public enum RideStatus {
    // 모집 중
    RECRUITING,
    // 인원 다 참
    FULL,
    // 출발 시간 지남
    DEPARTED,
    // 동승 완료 (후기 작성 가능)
    COMPLETED,
    // 방장이 취소
    CANCELED
}
