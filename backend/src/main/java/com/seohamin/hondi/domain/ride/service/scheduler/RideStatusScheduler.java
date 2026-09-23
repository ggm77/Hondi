package com.seohamin.hondi.domain.ride.service.scheduler;

import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 출발 시간이 지난 모집글의 상태를 주기적으로 바꾸는 스케줄러
 * RECRUITING, FULL -> DEPARTED (출발 시간 지남)
 * DEPARTED -> COMPLETED (출발 후 일정 시간 지남, 후기 작성 가능)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RideStatusScheduler {

    //출발 후 완료 처리까지 걸리는 시간
    private static final long COMPLETE_AFTER_HOURS = 3;

    private final RideRepository rideRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void updateRideStatus() {
        final LocalDateTime now = LocalDateTime.now();

        // 1) 출발 시간 지난 글 DEPARTED로 변경
        final int departed = rideRepository.bulkUpdateStatus(
                List.of(RideStatus.RECRUITING, RideStatus.FULL),
                RideStatus.DEPARTED,
                now
        );

        // 2) 출발 후 일정 시간 지난 글 COMPLETED로 변경
        final int completed = rideRepository.bulkUpdateStatus(
                List.of(RideStatus.DEPARTED),
                RideStatus.COMPLETED,
                now.minusHours(COMPLETE_AFTER_HOURS)
        );

        if(departed > 0 || completed > 0){
            log.info("Ride status updated. departed={}, completed={}", departed, completed);
        }
    }
}
