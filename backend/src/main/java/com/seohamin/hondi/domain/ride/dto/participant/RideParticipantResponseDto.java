package com.seohamin.hondi.domain.ride.dto.participant;

import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RideParticipantResponseDto {
    private final Long id;
    private final Long rideId;
    private final UserSimpleResponseDto user;
    private final ParticipantStatus status;
    private final String message;
    private final LocalDateTime createdAt;

    public RideParticipantResponseDto(final RideParticipant rideParticipant) {
        this.id = rideParticipant.getId();
        this.rideId = rideParticipant.getRide().getId();
        this.user = new UserSimpleResponseDto(rideParticipant.getUser());
        this.status = rideParticipant.getStatus();
        this.message = rideParticipant.getMessage();
        this.createdAt = rideParticipant.getCreatedAt();
    }
}
