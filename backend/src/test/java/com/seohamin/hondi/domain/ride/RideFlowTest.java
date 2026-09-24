package com.seohamin.hondi.domain.ride;

import com.jayway.jsonpath.JsonPath;
import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RideFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;

    private User host;
    private User guest;
    private User other;
    private String departureAt;

    @BeforeEach
    void setUp() {
        host = testAuthHelper.createUser("방장", Gender.FEMALE);
        guest = testAuthHelper.createUser("게스트", Gender.FEMALE);
        other = testAuthHelper.createUser("다른사람", Gender.MALE);
        departureAt = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    //공항 -> 성산일출봉 모집글 생성
    private long createRide(final int capacity) throws Exception {
        final String response = mockMvc.perform(post("/api/v1/ride")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originName":"제주국제공항","originLat":33.507000,"originLon":126.493000,
                                  "destName":"성산일출봉","destLat":33.458100,"destLon":126.942500,
                                  "departureAt":"%s","capacity":%d,"memo":"3번 게이트 앞"
                                }
                                """.formatted(departureAt, capacity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECRUITING"))
                .andExpect(jsonPath("$.currentCount").value(1))
                .andExpect(jsonPath("$.myStatus").value("HOST"))
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions join(final long rideId, final User user) throws Exception {
        return mockMvc.perform(post("/api/v1/ride/" + rideId + "/participant")
                .header("Authorization", testAuthHelper.bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"같이 가요\"}"));
    }

    private long joinAndGetId(final long rideId, final User user) throws Exception {
        final String response = join(rideId, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions decide(final long rideId, final long participantId, final User user, final String decision) throws Exception {
        return mockMvc.perform(patch("/api/v1/ride/" + rideId + "/participant/" + participantId)
                .header("Authorization", testAuthHelper.bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + decision + "\"}"));
    }

    @Test
    void 비슷한_경로를_검색하면_추천된다() throws Exception {
        final long rideId = createRide(4);

        //공항 근처에서 성산 근처로 가는 사람이 검색
        mockMvc.perform(get("/api/v1/rides/match")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .param("originLat", "33.510000").param("originLon", "126.499000")
                        .param("destLat", "33.460000").param("destLon", "126.940000")
                        .param("departureAt", departureAt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rides", hasSize(1)))
                .andExpect(jsonPath("$.rides[0].id").value(rideId))
                .andExpect(jsonPath("$.rides[0].score").exists());

        //애월로 가는 사람에게는 추천 안됨
        mockMvc.perform(get("/api/v1/rides/match")
                        .header("Authorization", testAuthHelper.bearer(guest))
                        .param("originLat", "33.507000").param("originLon", "126.493000")
                        .param("destLat", "33.463600").param("destLon", "126.331000")
                        .param("departureAt", departureAt))
                .andExpect(jsonPath("$.rides", hasSize(0)));
    }

    @Test
    void 신청_수락하면_인원이_차고_나가면_다시_모집() throws Exception {
        final long rideId = createRide(2);
        final long participantId = joinAndGetId(rideId, guest);

        //중복 신청 불가
        join(rideId, guest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RIDE_ALREADY_REQUESTED"));

        //방장이 아니면 수락 불가
        decide(rideId, participantId, guest, "ACCEPTED")
                .andExpect(status().isForbidden());

        //방장이 수락 -> 인원 다 참
        decide(rideId, participantId, host, "ACCEPTED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/v1/ride/" + rideId).header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$.status").value("FULL"))
                .andExpect(jsonPath("$.currentCount").value(2))
                .andExpect(jsonPath("$.myStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.members[0].nickname").value("게스트"));

        //다 찼으면 신청 불가
        join(rideId, other)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RIDE_FULL"));

        //참여자가 나가면 다시 모집 중
        mockMvc.perform(delete("/api/v1/ride/" + rideId + "/participant/me")
                        .header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/ride/" + rideId).header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(jsonPath("$.status").value("RECRUITING"))
                .andExpect(jsonPath("$.currentCount").value(1));

        //나갔던 사람은 다시 신청 가능
        join(rideId, guest).andExpect(status().isOk());
    }

    @Test
    void 거절된_사람은_다시_신청_불가() throws Exception {
        final long rideId = createRide(4);
        final long participantId = joinAndGetId(rideId, other);

        decide(rideId, participantId, host, "REJECTED")
                .andExpect(jsonPath("$.status").value("REJECTED"));

        join(rideId, other)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RIDE_REJECTED"));
    }

    @Test
    void 방장은_모집글을_수정하고_취소할_수_있다() throws Exception {
        final long rideId = createRide(4);

        mockMvc.perform(patch("/api/v1/ride/" + rideId)
                        .header("Authorization", testAuthHelper.bearer(host))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacity\":3,\"memo\":\"택시 승강장 2번\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.memo").value("택시 승강장 2번"));

        mockMvc.perform(delete("/api/v1/ride/" + rideId).header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/ride/" + rideId).header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(status().isNoContent());

        join(rideId, guest)
                .andExpect(jsonPath("$.code").value("RIDE_NOT_RECRUITING"));
    }

    @Test
    void 제주_밖은_등록_불가() throws Exception {
        mockMvc.perform(post("/api/v1/ride")
                        .header("Authorization", testAuthHelper.bearer(host))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originName":"서울역","originLat":37.554600,"originLon":126.970600,
                                  "destName":"성산일출봉","destLat":33.458100,"destLon":126.942500,
                                  "departureAt":"%s","capacity":4
                                }
                                """.formatted(departureAt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUT_OF_SERVICE_AREA"));
    }

    @Test
    void 내_모집글과_참여글_목록() throws Exception {
        final long rideId = createRide(4);
        joinAndGetId(rideId, guest);

        mockMvc.perform(get("/api/v1/rides/me").header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(jsonPath("$.rides", hasSize(1)))
                .andExpect(jsonPath("$.rides[0].myStatus").value("HOST"));

        mockMvc.perform(get("/api/v1/rides/me").header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$.rides", hasSize(1)))
                .andExpect(jsonPath("$.rides[0].myStatus").value("REQUESTED"));

        mockMvc.perform(get("/api/v1/ride/" + rideId + "/participants").header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value("같이 가요"));
    }
}
