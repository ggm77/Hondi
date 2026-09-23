package com.seohamin.hondi.domain.ride.dto.participant;

import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 참여 신청 요청 DTO
 */
@Getter
public class RideParticipantRequestDto {

    //방장에게 남기는 메세지 (선택)
    @Size(max = 200)
    private String message;
}
