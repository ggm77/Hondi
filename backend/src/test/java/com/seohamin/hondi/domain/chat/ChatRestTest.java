package com.seohamin.hondi.domain.chat;

import com.seohamin.hondi.domain.ride.entity.GenderPolicy;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.support.TestAuthHelper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private RideRepository rideRepository;

    private User host;
    private User guest;
    private User stranger;
    private Ride ride;

    @BeforeEach
    void setUp() throws Exception {
        host = testAuthHelper.createUser("채팅방장", Gender.MALE);
        guest = testAuthHelper.createUser("채팅게스트", Gender.MALE);
        stranger = testAuthHelper.createUser("모르는사람", Gender.MALE);
        ride = rideRepository.save(Ride.builder()
                .host(host)
                .originName("제주국제공항").originLat(new BigDecimal("33.507000")).originLon(new BigDecimal("126.493000"))
                .destName("협재해수욕장").destLat(new BigDecimal("33.394000")).destLon(new BigDecimal("126.239000"))
                .departureAt(LocalDateTime.now().plusHours(1))
                .capacity(3)
                .genderPolicy(GenderPolicy.ANY)
                .build());

        //게스트 참여 신청 후 수락
        final String response = mockMvc.perform(post("/api/v1/ride/" + ride.getId() + "/participant")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse().getContentAsString();
        final long participantId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(patch("/api/v1/ride/" + ride.getId() + "/participant/" + participantId)
                        .header("Authorization", testAuthHelper.bearer(host))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk());
    }

    private ResultActions send(final User user, final String message) throws Exception {
        return mockMvc.perform(post("/api/v1/chat/room/" + ride.getId() + "/messages")
                .header("Authorization", testAuthHelper.bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + message + "\"}"));
    }

    @Test
    void 멤버는_채팅을_보내고_동기화할_수_있다() throws Exception {
        send(host, "3번 게이트 앞에서 만나요").andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MESSAGE"))
                .andExpect(jsonPath("$.sender.nickname").value("채팅방장"));
        send(guest, "넵!").andExpect(status().isOk());

        //입장 시스템 메세지 + 채팅 2개
        mockMvc.perform(get("/api/v1/chat/room/" + ride.getId() + "/messages/sync")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chats", hasSize(3)))
                .andExpect(jsonPath("$.chats[0].type").value("ENTER"))
                .andExpect(jsonPath("$.chats[0].sender").doesNotExist())
                .andExpect(jsonPath("$.chats[2].message").value("넵!"))
                .andExpect(jsonPath("$.hasMore").value(false));

        //size로 나눠서 가져오기
        final String firstPage = mockMvc.perform(get("/api/v1/chat/room/" + ride.getId() + "/messages/sync")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .param("size", "2"))
                .andExpect(jsonPath("$.chats", hasSize(2)))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andReturn().getResponse().getContentAsString();
        final long nextCursor = ((Number) JsonPath.read(firstPage, "$.nextCursor")).longValue();

        mockMvc.perform(get("/api/v1/chat/room/" + ride.getId() + "/messages/sync")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .param("lastChatId", String.valueOf(nextCursor)))
                .andExpect(jsonPath("$.chats", hasSize(1)))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void 멤버가_아니면_채팅_불가() throws Exception {
        send(stranger, "안녕하세요")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CHAT_ROOM_NOT_JOINED"));

        mockMvc.perform(get("/api/v1/chat/room/" + ride.getId() + "/messages/sync")
                        .header("Authorization", testAuthHelper.bearer(stranger)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 나간_참여자는_채팅_불가하고_퇴장_메세지가_남는다() throws Exception {
        mockMvc.perform(delete("/api/v1/ride/" + ride.getId() + "/participant/me")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isNoContent());

        send(guest, "저 나갔어요").andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/chat/room/" + ride.getId() + "/messages/sync")
                        .header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(jsonPath("$.chats[1].type").value("EXIT"))
                .andExpect(jsonPath("$.chats[1].message").value("채팅게스트님이 나갔어요."));
    }

    @Test
    void 참여중인_채팅방_목록() throws Exception {
        send(host, "마지막 메세지").andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/chat/rooms").header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rideId").value(ride.getId()))
                .andExpect(jsonPath("$[0].memberCount").value(2))
                .andExpect(jsonPath("$[0].lastChat.message").value("마지막 메세지"));

        mockMvc.perform(get("/api/v1/chat/rooms").header("Authorization", testAuthHelper.bearer(stranger)))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
