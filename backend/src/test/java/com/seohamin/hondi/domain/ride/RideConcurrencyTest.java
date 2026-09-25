package com.seohamin.hondi.domain.ride;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.ride.service.participant.RideParticipantService;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시에 여러 명이 참여해도 인원이 초과되지 않는지 확인
 * 여러 스레드에서 커밋이 필요해서 @Transactional 없이 실행하고 직접 정리함
 */
@SpringBootTest
class RideConcurrencyTest {

    @Autowired
    private RideParticipantService rideParticipantService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideParticipantRepository rideParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @AfterEach
    void tearDown() {
        rideParticipantRepository.deleteAll();
        rideRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 동시에_참여해도_정원을_넘지_않는다() throws Exception {
        // 1) 정원 2명(방장 포함) 모집글과 참여하려는 유저 5명
        final User host = testAuthHelper.createUser("동시성방장");
        final Ride ride = rideRepository.save(Ride.builder()
                .host(host)
                .originName("제주국제공항").originLat(new BigDecimal("33.507000")).originLon(new BigDecimal("126.493000"))
                .destName("성산일출봉").destLat(new BigDecimal("33.458100")).destLon(new BigDecimal("126.942500"))
                .departureAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .capacity(2)
                .build());

        final int requestCount = 5;
        final List<Long> guestIds = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            guestIds.add(testAuthHelper.createUser("동시성게스트" + i).getId());
        }

        // 2) 모두 동시에 참여
        final ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(requestCount);
        final AtomicInteger success = new AtomicInteger();

        for (final Long guestId : guestIds) {
            executor.submit(() -> {
                try {
                    start.await();
                    rideParticipantService.join(ride.getId(), guestId);
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // 인원 초과로 실패하는 경우
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();

        // 3) 한 명만 참여되고 인원은 정원과 같아야 함
        final Ride result = rideRepository.findById(ride.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(1);
        assertThat(result.getCurrentCount()).isEqualTo(2);
    }
}
