package com.seohamin.hondi.domain.ride.controller.list;

import com.seohamin.hondi.domain.ride.dto.list.RideListResponseDto;
import com.seohamin.hondi.domain.ride.service.list.RideListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RideListController {

    private final RideListService rideListService;

    //동승 모집글 추천 API
    //출발지, 도착지 좌표 넣으면 비슷한 경로, 시간대 모집글을 점수 순으로 줌
    @GetMapping("/rides/match")
    public ResponseEntity<RideListResponseDto> matchRides(
            @RequestParam final BigDecimal originLat,
            @RequestParam final BigDecimal originLon,
            @RequestParam final BigDecimal destLat,
            @RequestParam final BigDecimal destLon,
            @RequestParam(required = false) final Instant departureAt,
            @RequestParam(required = false, defaultValue = "20") final Integer size,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideListService.matchRides(
                originLat,
                originLon,
                destLat,
                destLon,
                departureAt,
                size,
                userId
        ));
    }

    //출발 예정인 모집 중인 글 목록 API (출발 시간 순)
    @GetMapping("/rides")
    public ResponseEntity<RideListResponseDto> getUpcomingRides(
            @RequestParam(required = false, defaultValue = "0") final Integer page,
            @RequestParam(required = false, defaultValue = "20") final Integer size,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideListService.getUpcomingRides(page, size, userId));
    }

    //내가 만든 글, 참여 중인 글 목록 API
    @GetMapping("/rides/me")
    public ResponseEntity<RideListResponseDto> getMyRides(
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(rideListService.getMyRides(userId));
    }
}
