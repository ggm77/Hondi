package com.seohamin.hondi.domain.ride.controller.participant;

import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantResponseDto;
import com.seohamin.hondi.domain.ride.service.participant.RideParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RideParticipantController {

    private final RideParticipantService rideParticipantService;

    //모집글 참여 API (최대 인원 내라면 바로 참여)
    @PostMapping("/ride/{id}/participant")
    public ResponseEntity<RideParticipantResponseDto> join(
            @PathVariable("id") final Long rideId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideParticipantService.join(rideId, userId));
    }

    //모집글 나가기 API
    @DeleteMapping("/ride/{id}/participant/me")
    public ResponseEntity<Void> leave(
            @PathVariable("id") final Long rideId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        rideParticipantService.leave(rideId, userId);

        return ResponseEntity.noContent().build();
    }
}
