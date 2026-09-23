package com.seohamin.hondi.domain.ride.controller;

import com.seohamin.hondi.domain.ride.dto.RideRequestDto;
import com.seohamin.hondi.domain.ride.dto.RideResponseDto;
import com.seohamin.hondi.domain.ride.service.RideService;
import com.seohamin.hondi.global.validation.Create;
import com.seohamin.hondi.global.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    //동승 모집글 생성 API
    @PostMapping("/ride")
    public ResponseEntity<RideResponseDto> createRide(
            @AuthenticationPrincipal final String userIdStr,
            @Validated(Create.class) @RequestBody final RideRequestDto rideRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideService.createRide(rideRequestDto, userId));
    }

    //동승 모집글 조회 API
    @GetMapping("/ride/{id}")
    public ResponseEntity<RideResponseDto> getRide(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideService.getRide(id, userId));
    }

    //동승 모집글 수정 API (출발 시간, 최대 인원, 메모)
    @PatchMapping("/ride/{id}")
    public ResponseEntity<RideResponseDto> updateRide(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr,
            @Validated(Update.class) @RequestBody final RideRequestDto rideRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideService.updateRide(id, rideRequestDto, userId));
    }

    //동승 모집글 취소 API
    @DeleteMapping("/ride/{id}")
    public ResponseEntity<Void> cancelRide(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        rideService.cancelRide(id, userId);

        return ResponseEntity.noContent().build();
    }
}
