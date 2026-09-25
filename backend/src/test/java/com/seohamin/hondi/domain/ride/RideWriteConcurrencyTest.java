package com.seohamin.hondi.domain.ride;

import com.seohamin.hondi.domain.chat.dto.ChatMessageRequestDto;
import com.seohamin.hondi.domain.chat.dto.ChatMessageResponseDto;
import com.seohamin.hondi.domain.chat.repository.ChatMessageRepository;
import com.seohamin.hondi.domain.chat.repository.ChatReadStatusRepository;
import com.seohamin.hondi.domain.chat.service.ChatService;
import com.seohamin.hondi.domain.ride.dto.RideRequestDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.ride.service.RideService;
import com.seohamin.hondi.domain.ride.service.participant.RideParticipantService;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;

/** 서비스 호출 도중 DB 조회/커밋을 지연해 요청이 실제로 겹치는 경우를 검증한다. */
@SpringBootTest
class RideWriteConcurrencyTest {

    @MockitoSpyBean
    private RideRepository rideRepository;

    @MockitoSpyBean
    private ChatReadStatusRepository chatReadStatusRepository;

    @Autowired private RideParticipantRepository participantRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RideService rideService;
    @Autowired private RideParticipantService participantService;
    @Autowired private ChatService chatService;
    @Autowired private TestAuthHelper auth;
    @Autowired private PlatformTransactionManager transactionManager;

    private final JsonMapper mapper = new JsonMapper();

    @AfterEach
    void tearDown() {
        chatReadStatusRepository.deleteAllInBatch();
        chatMessageRepository.deleteAllInBatch();
        participantRepository.deleteAllInBatch();
        rideRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void 메모_수정과_참여가_겹쳐도_인원수가_유지된다() throws Exception {
        final User host = auth.createUser("수정방장");
        final User guest = auth.createUser("참여자");
        final Ride ride = createRide(host, 2);

        overlap(pauseRideRead(ride.getId()),
                () -> rideService.updateRide(ride.getId(), update("{\"memo\":\"새 합류 지점\"}"), host.getId()),
                () -> participantService.join(ride.getId(), guest.getId()));

        assertCount(ride, 2);
        assertThat(rideRepository.findById(ride.getId()).orElseThrow().getMemo()).isEqualTo("새 합류 지점");
        final User extra = auth.createUser("추가참여자");
        assertThatThrownBy(() -> participantService.join(ride.getId(), extra.getId()))
                .isInstanceOfSatisfying(CustomException.class,
                        ex -> assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.RIDE_FULL));
    }

    @Test
    void 정원_축소와_참여가_겹쳐도_정원을_초과하지_않는다() throws Exception {
        final User host = auth.createUser("정원방장");
        final User guest = auth.createUser("정원참여자");
        final Ride ride = createRide(host, 4);

        overlap(pauseRideRead(ride.getId()),
                () -> rideService.updateRide(ride.getId(), update("{\"capacity\":2}"), host.getId()),
                () -> participantService.join(ride.getId(), guest.getId()));

        assertCount(ride, 2);
        assertThat(rideRepository.findById(ride.getId()).orElseThrow().getCapacity()).isEqualTo(2);
    }

    @Test
    void 메모_수정과_탈퇴가_겹쳐도_인원수가_유지된다() throws Exception {
        final User host = auth.createUser("탈퇴방장");
        final User guest = auth.createUser("탈퇴참여자");
        final Ride ride = createRide(host, 4);
        participantService.join(ride.getId(), guest.getId());

        overlap(pauseRideRead(ride.getId()),
                () -> rideService.updateRide(ride.getId(), update("{\"memo\":\"새 합류 지점\"}"), host.getId()),
                () -> participantService.leave(ride.getId(), guest.getId()));

        assertCount(ride, 1);
    }

    @Test
    void 먼저_보낸_메시지의_커밋이_늦어도_폴링에서_누락되지_않는다() throws Exception {
        final User host = auth.createUser("채팅방장");
        final User guest = auth.createUser("채팅참여자");
        final Ride ride = createRide(host, 4);
        participantService.join(ride.getId(), guest.getId());
        final Pause pause = new Pause();
        final long[] messageIds = new long[2];

        overlap(pause, () -> new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            messageIds[0] = send(ride, host, "먼저 보낸 메시지").getId();
            pause.hold();
        }), () -> messageIds[1] = send(ride, guest, "다음 메시지").getId());

        assertThat(messageIds[0]).isLessThan(messageIds[1]);
        final var firstPage = chatService.getMessages(ride.getId(), 0L, null, 1, guest.getId());
        assertThat(firstPage.getMessages()).extracting(ChatMessageResponseDto::getId).containsExactly(messageIds[0]);
        assertThat(firstPage.isHasNext()).isTrue();
        final var secondPage = chatService.getMessages(ride.getId(), messageIds[0], null, 1, guest.getId());
        assertThat(secondPage.getMessages()).extracting(ChatMessageResponseDto::getId).containsExactly(messageIds[1]);
        assertThat(secondPage.isHasNext()).isFalse();
    }

    @Test
    void 읽음_요청이_겹쳐도_마지막_읽은_위치가_되돌아가지_않는다() throws Exception {
        final User host = auth.createUser("읽음방장");
        final Ride ride = createRide(host, 4);
        final long firstId = send(ride, host, "첫번째").getId();
        final long secondId = send(ride, host, "두번째").getId();
        final long thirdId = send(ride, host, "세번째").getId();
        chatService.markRead(ride.getId(), firstId, host.getId());

        overlap(pauseReadStatus(ride.getId(), host.getId()),
                () -> chatService.markRead(ride.getId(), secondId, host.getId()),
                () -> chatService.markRead(ride.getId(), thirdId, host.getId()));

        //뒤늦게 도착한 과거 읽음 요청도 최신 위치를 유지해야 함
        chatService.markRead(ride.getId(), firstId, host.getId());
        assertThat(chatReadStatusRepository.findByRideIdAndUserId(ride.getId(), host.getId()).orElseThrow()
                .getLastReadMessageId()).isEqualTo(thirdId);
        assertThat(chatService.getMyChatRooms(host.getId()).getRooms().getFirst().getUnreadCount()).isZero();
    }

    @Test
    void 최초_읽음_요청이_겹쳐도_한_개의_최신_기록만_저장된다() throws Exception {
        final User host = auth.createUser("최초읽음방장");
        final Ride ride = createRide(host, 4);
        final long firstId = send(ride, host, "첫번째").getId();
        final long lastId = send(ride, host, "마지막").getId();

        overlap(pauseReadStatus(ride.getId(), host.getId()),
                () -> chatService.markRead(ride.getId(), firstId, host.getId()),
                () -> chatService.markRead(ride.getId(), lastId, host.getId()));

        assertThat(chatReadStatusRepository.count()).isEqualTo(1);
        assertThat(chatReadStatusRepository.findByRideIdAndUserId(ride.getId(), host.getId()).orElseThrow()
                .getLastReadMessageId()).isEqualTo(lastId);
    }

    private Ride createRide(final User host, final int capacity) {
        return rideRepository.save(Ride.builder().host(host)
                .originName("제주공항").originLat(new BigDecimal("33.507000")).originLon(new BigDecimal("126.493000"))
                .destName("성산일출봉").destLat(new BigDecimal("33.458100")).destLon(new BigDecimal("126.942500"))
                .departureAt(Instant.now().plusSeconds(3600)).capacity(capacity).build());
    }

    private RideRequestDto update(final String json) {
        return mapper.readValue(json, RideRequestDto.class);
    }

    private ChatMessageResponseDto send(final Ride ride, final User sender, final String content) {
        return chatService.sendMessage(ride.getId(),
                mapper.readValue("{\"content\":\"" + content + "\"}", ChatMessageRequestDto.class), sender.getId());
    }

    private void assertCount(final Ride ride, final int expected) {
        assertThat(rideRepository.findById(ride.getId()).orElseThrow().getCurrentCount()).isEqualTo(expected);
        assertThat(participantRepository.findByRideIdWithUser(ride.getId())).hasSize(expected - 1);
    }

    private Pause pauseRideRead(final Long rideId) {
        final Pause pause = new Pause();
        //일반 조회로 되돌리는 회귀도 같은 요청 순서로 검증
        doAnswer(pause::afterRead).when(rideRepository).findById(rideId);
        doAnswer(pause::afterRead).when(rideRepository).findByIdForUpdate(rideId);
        return pause;
    }

    private Pause pauseReadStatus(final Long rideId, final Long userId) {
        final Pause pause = new Pause();
        doAnswer(pause::afterRead).when(chatReadStatusRepository).findByRideIdAndUserId(rideId, userId);
        return pause;
    }

    private void overlap(final Pause pause, final Runnable first, final Runnable second) throws Exception {
        try (final var executor = Executors.newFixedThreadPool(2)) {
            final var firstResult = executor.submit(first);
            try {
                if (!pause.reached.await(2, TimeUnit.SECONDS)) {
                    firstResult.get(10, TimeUnit.SECONDS);
                    throw new AssertionError("첫 요청이 지연 지점에 도착하지 않았습니다.");
                }
                final CountDownLatch secondStarted = new CountDownLatch(1);
                final var secondResult = executor.submit(() -> {
                    secondStarted.countDown();
                    second.run();
                });
                try {
                    await(secondStarted);
                    //먼저 시작한 트랜잭션이 끝나기 전에는 다음 쓰기가 완료되면 안 됨
                    assertThrows(TimeoutException.class, () -> secondResult.get(250, TimeUnit.MILLISECONDS));
                } finally {
                    pause.release.countDown();
                }
                firstResult.get(10, TimeUnit.SECONDS);
                secondResult.get(10, TimeUnit.SECONDS);
            } finally {
                pause.release.countDown();
            }
        }
    }

    private static void await(final CountDownLatch latch) {
        try {
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
    }

    private static class Pause {
        private final AtomicBoolean firstRead = new AtomicBoolean(true);
        private final CountDownLatch reached = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        private Object afterRead(final InvocationOnMock invocation) throws Throwable {
            //Spring Data 인터페이스의 기본 Answer가 실제 저장소 프록시로 위임한다.
            final Object result = mockingDetails(invocation.getMock()).getMockCreationSettings()
                    .getDefaultAnswer().answer(invocation);
            if (firstRead.compareAndSet(true, false)) {
                hold();
            }
            return result;
        }

        private void hold() {
            reached.countDown();
            await(release);
        }
    }
}
