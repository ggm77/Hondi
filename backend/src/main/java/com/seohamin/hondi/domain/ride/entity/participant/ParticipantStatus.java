package com.seohamin.hondi.domain.ride.entity.participant;

public enum ParticipantStatus {
    // 참여 신청
    REQUESTED,
    // 방장이 수락
    ACCEPTED,
    // 방장이 거절
    REJECTED,
    // 참여자가 나감 (신청 취소 포함)
    LEFT
}
