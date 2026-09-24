package com.seohamin.hondi.domain.ride;

import com.seohamin.hondi.domain.chat.repository.ChatRepository;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantDecisionRequestDto;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantRequestDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시에 여러 신청을 수락해도 인원이 초과되지 않는지 확인
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

    @Autowired
    private ChatRepository chatRepository;

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll();
        rideParticipantRepository.deleteAll();
        rideRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 동시에_수락해도_정원을_넘지_않는다() throws Exception {
        // 1) 정원 2명(방장 포함) 모집글과 신청자 5명
        final User host = testAuthHelper.createUser("동시성방장");
        final Ride ride = rideRepository.save(Ride.builder()
                .host(host)
                .originName("제주국제공항").originLat(new BigDecimal("33.507000")).originLon(new BigDecimal("126.493000"))
                .destName("성산일출봉").destLat(new BigDecimal("33.458100")).destLon(new BigDecimal("126.942500"))
                .departureAt(LocalDateTime.now().plusHours(1))
                .capacity(2)
                .build());

        final int requestCount = 5;
        final List<Long> participantIds = new java.util.ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            final User guest = testAuthHelper.createUser("동시성게스트" + i);
            participantIds.add(rideParticipantService
                    .requestJoin(ride.getId(), new RideParticipantRequestDto(), guest.getId())
                    .getId());
        }

        // 2) 모든 신청을 동시에 수락
        final RideParticipantDecisionRequestDto accept = new RideParticipantDecisionRequestDto();
        ReflectionTestUtils.setField(accept, "status", ParticipantStatus.ACCEPTED);

        final ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(requestCount);
        final AtomicInteger success = new AtomicInteger();

        for (final Long participantId : participantIds) {
            executor.submit(() -> {
                try {
                    start.await();
                    rideParticipantService.decide(ride.getId(), participantId, accept, host.getId());
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

        // 3) 한 명만 수락되고 인원은 정원과 같아야 함
        final Ride result = rideRepository.findById(ride.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(1);
        assertThat(result.getCurrentCount()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(RideStatus.FULL);
    }
}
