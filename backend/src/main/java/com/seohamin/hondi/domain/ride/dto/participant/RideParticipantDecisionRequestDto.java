package com.seohamin.hondi.domain.ride.dto.participant;

import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 방장이 참여 신청을 수락/거절할 때 쓰는 DTO
 * status는 ACCEPTED 또는 REJECTED만 가능
 */
@Getter
public class RideParticipantDecisionRequestDto {

    @NotNull
    private ParticipantStatus status;
}
