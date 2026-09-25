package com.seohamin.hondi.domain.chat;

import com.jayway.jsonpath.JsonPath;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;

    private User host;
    private User guest;
    private User other;

    @BeforeEach
    void setUp() {
        host = testAuthHelper.createUser("방장");
        guest = testAuthHelper.createUser("게스트");
        other = testAuthHelper.createUser("다른사람");
    }

    //공항 -> 성산일출봉 모집글 생성
    private long createRide() throws Exception {
        final String departureAt = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS).toString();

        final String response = mockMvc.perform(post("/api/v1/ride")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originName":"제주국제공항","originLat":33.507000,"originLon":126.493000,
                                  "destName":"성산일출봉","destLat":33.458100,"destLon":126.942500,
                                  "departureAt":"%s","capacity":4,"memo":"3번 게이트 앞"
                                }
                                """.formatted(departureAt)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void join(final long rideId, final User user) throws Exception {
        mockMvc.perform(post("/api/v1/ride/" + rideId + "/participant")
                        .header("Authorization", testAuthHelper.bearer(user)))
                .andExpect(status().isOk());
    }

    private long sendMessage(final long rideId, final User user, final String content) throws Exception {
        final String response = mockMvc.perform(post("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void 참여자만_채팅을_주고받을_수_있다() throws Exception {
        final long rideId = createRide();

        //참여자가 아니면 조회, 전송 모두 불가
        mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(other)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_RIDE_MEMBER"));

        mockMvc.perform(post("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"안녕하세요\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_RIDE_MEMBER"));

        //방장은 바로 채팅 가능
        sendMessage(rideId, host, "출발지에서 뵐게요");

        //참여하면 채팅 가능
        join(rideId, guest);
        sendMessage(rideId, guest, "네 좋습니다");

        mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(2)))
                .andExpect(jsonPath("$.messages[0].content").value("출발지에서 뵐게요"))
                .andExpect(jsonPath("$.messages[0].sender.nickname").value("방장"))
                .andExpect(jsonPath("$.messages[1].content").value("네 좋습니다"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void afterId로_폴링하면_새_메시지만_온다() throws Exception {
        final long rideId = createRide();
        final long firstId = sendMessage(rideId, host, "첫번째");
        sendMessage(rideId, host, "두번째");
        sendMessage(rideId, host, "세번째");

        //첫 메시지 이후로 폴링하면 두, 세번째만 옴
        mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .param("afterId", String.valueOf(firstId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(2)))
                .andExpect(jsonPath("$.messages[0].content").value("두번째"))
                .andExpect(jsonPath("$.messages[1].content").value("세번째"))
                .andExpect(jsonPath("$.hasNext").value(false));

        //마지막 메시지 이후로 폴링하면 빈 목록
        mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .param("afterId", "999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(0)))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void beforeId로_과거_메시지를_스크롤한다() throws Exception {
        final long rideId = createRide();
        for (int i = 1; i <= 5; i++) {
            sendMessage(rideId, host, "메시지" + i);
        }

        //커서 없이 최신 2개 조회 -> 4, 5번째
        final String firstPage = mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(2)))
                .andExpect(jsonPath("$.messages[0].content").value("메시지4"))
                .andExpect(jsonPath("$.messages[1].content").value("메시지5"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andReturn().getResponse().getContentAsString();

        final long oldestIdInFirstPage = ((Number) JsonPath.read(firstPage, "$.messages[0].id")).longValue();

        //그 이전 2개 조회 -> 2, 3번째
        mockMvc.perform(get("/api/v1/chats/" + rideId + "/messages")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .param("beforeId", String.valueOf(oldestIdInFirstPage))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(2)))
                .andExpect(jsonPath("$.messages[0].content").value("메시지2"))
                .andExpect(jsonPath("$.messages[1].content").value("메시지3"))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void 읽음_처리하면_채팅방_목록의_안읽은_개수가_줄어든다() throws Exception {
        final long rideId = createRide();
        join(rideId, guest);

        sendMessage(rideId, host, "첫번째");
        final long lastId = sendMessage(rideId, host, "두번째");

        //게스트 입장에서는 2개 안읽음
        mockMvc.perform(get("/api/v1/chats/rooms")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms", hasSize(1)))
                .andExpect(jsonPath("$.rooms[0].rideId").value(rideId))
                .andExpect(jsonPath("$.rooms[0].lastMessage").value("두번째"))
                .andExpect(jsonPath("$.rooms[0].unreadCount").value(2));

        //마지막 메시지까지 읽음 처리
        mockMvc.perform(post("/api/v1/chats/" + rideId + "/read")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .param("lastMessageId", String.valueOf(lastId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/chats/rooms")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$.rooms[0].unreadCount").value(0));
    }
}
