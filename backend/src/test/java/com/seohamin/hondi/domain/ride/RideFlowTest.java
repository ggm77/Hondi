package com.seohamin.hondi.domain.ride;

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
import org.springframework.test.web.servlet.ResultActions;
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
        host = testAuthHelper.createUser("방장");
        guest = testAuthHelper.createUser("게스트");
        other = testAuthHelper.createUser("다른사람");
        departureAt = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS).toString();
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
                .header("Authorization", testAuthHelper.bearer(user)));
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
    void 참여하면_바로_인원이_차고_나가면_다시_모집() throws Exception {
        final long rideId = createRide(2);

        //방장 수락 없이 바로 참여
        join(rideId, guest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.nickname").value("게스트"));

        //중복 참여 불가
        join(rideId, guest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RIDE_ALREADY_JOINED"));

        //자기 글 참여 불가
        join(rideId, host)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CANNOT_JOIN_OWN_RIDE"));

        mockMvc.perform(get("/api/v1/ride/" + rideId).header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$.status").value("FULL"))
                .andExpect(jsonPath("$.currentCount").value(2))
                .andExpect(jsonPath("$.myStatus").value("JOINED"))
                .andExpect(jsonPath("$.members[0].nickname").value("게스트"));

        //다 찼으면 참여 불가
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

        //나갔던 사람도 다시 참여 가능
        join(rideId, guest).andExpect(status().isOk());

        //참여 중이 아니면 나가기 불가
        mockMvc.perform(delete("/api/v1/ride/" + rideId + "/participant/me")
                        .header("Authorization", testAuthHelper.bearer(other)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTICIPANT_NOT_EXIST"));
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
        join(rideId, guest).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/rides/me").header("Authorization", testAuthHelper.bearer(host)))
                .andExpect(jsonPath("$.rides", hasSize(1)))
                .andExpect(jsonPath("$.rides[0].myStatus").value("HOST"));

        mockMvc.perform(get("/api/v1/rides/me").header("Authorization", testAuthHelper.bearer(guest)))
                .andExpect(jsonPath("$.rides", hasSize(1)))
                .andExpect(jsonPath("$.rides[0].myStatus").value("JOINED"));
    }
}
