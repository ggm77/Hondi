package com.seohamin.hondi.domain.ride.controller.participant;

import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantDecisionRequestDto;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantRequestDto;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantResponseDto;
import com.seohamin.hondi.domain.ride.service.participant.RideParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RideParticipantController {

    private final RideParticipantService rideParticipantService;

    //참여 신청 API
    @PostMapping("/ride/{id}/participant")
    public ResponseEntity<RideParticipantResponseDto> requestJoin(
            @PathVariable("id") final Long rideId,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final RideParticipantRequestDto rideParticipantRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideParticipantService.requestJoin(rideId, rideParticipantRequestDto, userId));
    }

    //참여 신청자, 참여자 목록 조회 API (방장만)
    @GetMapping("/ride/{id}/participants")
    public ResponseEntity<List<RideParticipantResponseDto>> getParticipants(
            @PathVariable("id") final Long rideId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideParticipantService.getParticipants(rideId, userId));
    }

    //참여 신청 수락/거절 API (방장만)
    @PatchMapping("/ride/{id}/participant/{participantId}")
    public ResponseEntity<RideParticipantResponseDto> decide(
            @PathVariable("id") final Long rideId,
            @PathVariable final Long participantId,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final RideParticipantDecisionRequestDto decisionRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideParticipantService.decide(rideId, participantId, decisionRequestDto, userId));
    }

    //참여 신청 취소 or 나가기 API
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
