package com.seohamin.hondi.domain.ride.dto.participant;

import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RideParticipantResponseDto {
    private final Long id;
    private final Long rideId;
    private final UserSimpleResponseDto user;
    private final Instant createdAt;

    public RideParticipantResponseDto(final RideParticipant rideParticipant) {
        this.id = rideParticipant.getId();
        this.rideId = rideParticipant.getRide().getId();
        this.user = new UserSimpleResponseDto(rideParticipant.getUser());
        this.createdAt = rideParticipant.getCreatedAt();
    }
}
