package com.seohamin.hondi.domain.auth;

import com.jayway.jsonpath.JsonPath;
import com.seohamin.hondi.global.auth.kakao.client.KakaoAuthClient;
import com.seohamin.hondi.global.auth.kakao.dto.user.KakaoUserInfoResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KakaoLoginTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = new JsonMapper();

    @MockitoBean
    private KakaoAuthClient kakaoAuthClient;

    @BeforeEach
    void setUp() {
        //카카오 유저 정보 응답 흉내
        final KakaoUserInfoResponseDto userInfo = jsonMapper.readValue("""
                {
                  "id": 123456789,
                  "kakao_account": {
                    "email": "traveler@kakao.com",
                    "profile": {"nickname": "홍길동", "profile_image_url": "https://k.kakaocdn.net/p.jpg"}
                  }
                }
                """, KakaoUserInfoResponseDto.class);
        given(kakaoAuthClient.requestUserInfo(anyString())).willReturn(userInfo);
    }

    private String kakaoLogin() throws Exception {
        return mockMvc.perform(post("/api/v1/auth/oauth2/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accessToken":"kakao-access-token"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void 카카오_로그인시_카카오_닉네임으로_바로_가입된다() throws Exception {
        // 1) 첫 로그인시 바로 USER로 가입되고 카카오 닉네임이 저장됨
        final String loginResponse = kakaoLogin();
        final String accessToken = JsonPath.read(loginResponse, "$.accessToken");
        assertThat((String) JsonPath.read(loginResponse, "$.role")).isEqualTo("USER");

        // 2) 별도 회원가입 절차 없이 바로 다른 API 사용 가능
        mockMvc.perform(get("/api/v1/user/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("홍길동"))
                .andExpect(jsonPath("$.role").value("USER"));

        // 3) 같은 카카오 계정으로 다시 로그인해도 새로 가입되지 않고 USER 유지
        assertThat((String) JsonPath.read(kakaoLogin(), "$.role")).isEqualTo("USER");
    }

    @Test
    void 카카오_프로필에_닉네임이_없으면_로그인_실패() throws Exception {
        final KakaoUserInfoResponseDto userInfo = jsonMapper.readValue("""
                {
                  "id": 987654321,
                  "kakao_account": {
                    "email": "no-nickname@kakao.com",
                    "profile": {"profile_image_url": "https://k.kakaocdn.net/p.jpg"}
                  }
                }
                """, KakaoUserInfoResponseDto.class);
        given(kakaoAuthClient.requestUserInfo(anyString())).willReturn(userInfo);

        mockMvc.perform(post("/api/v1/auth/oauth2/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accessToken":"kakao-access-token"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("KAKAO_REQUEST_ERROR"));
    }

    @Test
    void 카카오_토큰이_없으면_실패() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth2/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 리프레시_토큰은_액세스_토큰으로_사용불가() throws Exception {
        final String loginResponse = kakaoLogin();
        final String accessToken = JsonPath.read(loginResponse, "$.accessToken");
        final String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());

        //액세스 토큰으로 재발급 요청하면 거절
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
