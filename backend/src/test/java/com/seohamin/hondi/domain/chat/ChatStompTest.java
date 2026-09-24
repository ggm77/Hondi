package com.seohamin.hondi.domain.chat;

import com.seohamin.hondi.domain.chat.dto.ChatRequestDto;
import com.seohamin.hondi.domain.chat.repository.ChatRepository;
import com.seohamin.hondi.domain.chat.service.ChatService;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 서버를 띄우고 STOMP로 접속해서 채팅이 전달되는지 확인
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatStompTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideParticipantRepository rideParticipantRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    private WebSocketStompClient stompClient;
    private User host;
    private User stranger;
    private Ride ride;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        host = testAuthHelper.createUser("스톰프방장", Gender.FEMALE);
        stranger = testAuthHelper.createUser("스톰프외부인", Gender.FEMALE);
        ride = rideRepository.save(Ride.builder()
                .host(host)
                .originName("제주국제공항").originLat(new BigDecimal("33.507000")).originLon(new BigDecimal("126.493000"))
                .destName("협재해수욕장").destLat(new BigDecimal("33.394000")).destLon(new BigDecimal("126.239000"))
                .departureAt(LocalDateTime.now().plusHours(1))
                .capacity(3)
                .build());
    }

    @AfterEach
    void tearDown() {
        stompClient.stop();
        chatRepository.deleteAll();
        rideParticipantRepository.deleteAll();
        rideRepository.deleteAll();
        userRepository.deleteAll();
    }

    //접속 후 에러 프레임은 errors 큐에 담음
    private StompSession connect(final User user, final BlockingQueue<String> errors) throws Exception {
        final StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", testAuthHelper.bearer(user));

        return stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                (WebSocketHttpHeaders) null,
                connectHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(final StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(final StompHeaders headers, final Object payload) {
                        errors.add(new String((byte[]) payload, StandardCharsets.UTF_8));
                    }
                }
        ).get(5, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private StompFrameHandler collectTo(final BlockingQueue<Map<String, Object>> received) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(final StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(final StompHeaders headers, final Object payload) {
                received.add((Map<String, Object>) payload);
            }
        };
    }

    @Test
    void 구독한_멤버는_채팅을_실시간으로_받는다() throws Exception {
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();
        final BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();

        final StompSession session = connect(host, errors);
        session.subscribe("/topic/chat.room." + ride.getId(), collectTo(received));
        //구독이 처리될 때까지 잠깐 대기
        Thread.sleep(300);

        final ChatRequestDto request = new ChatRequestDto();
        ReflectionTestUtils.setField(request, "message", "공항 3번 게이트!");
        chatService.sendChat(ride.getId(), request, host.getId());

        final Map<String, Object> chat = received.poll(5, TimeUnit.SECONDS);
        assertThat(chat).isNotNull();
        assertThat(chat.get("message")).isEqualTo("공항 3번 게이트!");
        assertThat(errors).isEmpty();
    }

    @Test
    void 멤버가_아니면_구독이_거절된다() throws Exception {
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();
        final BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();

        final StompSession session = connect(stranger, errors);
        session.subscribe("/topic/chat.room." + ride.getId(), collectTo(received));

        final String error = errors.poll(5, TimeUnit.SECONDS);
        assertThat(error).contains("CHAT_ROOM_NOT_JOINED");
    }

    @Test
    void 토큰이_없으면_접속이_거절된다() throws Exception {
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();

        stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                (WebSocketHttpHeaders) null,
                new StompHeaders(),
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(final StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(final StompHeaders headers, final Object payload) {
                        errors.add(new String((byte[]) payload, StandardCharsets.UTF_8));
                    }
                }
        );

        final String error = errors.poll(5, TimeUnit.SECONDS);
        assertThat(error).contains("UNAUTHORIZED");
    }
}
