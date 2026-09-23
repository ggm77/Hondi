package com.seohamin.hondi.domain.auth;

import com.jayway.jsonpath.JsonPath;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.global.infra.google.GoogleOauth2Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class Oauth2LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleOauth2Client googleOauth2Client;

    @BeforeEach
    void setUp() {
        given(googleOauth2Client.fetchUser(anyString())).willReturn(UserOauth2AccountsRequestDto.builder()
                .provider("google")
                .providerUserId("google-sub-1")
                .email("traveler@gmail.com")
                .name("홍길동")
                .build());
    }

    private String googleLogin() throws Exception {
        return mockMvc.perform(post("/api/v1/auth/oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"auth-code","provider":"google"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void 구글_로그인_회원가입_토큰재발급_흐름() throws Exception {
        // 1) 첫 로그인은 NOT_REGISTERED
        final String loginResponse = googleLogin();
        final String accessToken = JsonPath.read(loginResponse, "$.accessToken");
        final String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");
        assertThat((String) JsonPath.read(loginResponse, "$.role")).isEqualTo("NOT_REGISTERED");

        // 2) 회원가입 전에는 다른 API 사용 불가
        mockMvc.perform(get("/api/v1/user/1").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());

        // 3) 회원가입 완료
        mockMvc.perform(post("/api/v1/user")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"제주여행자","gender":"FEMALE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.name").value("홍길동"));

        // 4) 토큰 재발급하면 USER 권한으로 사용 가능
        final String refreshed = mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final String userToken = JsonPath.read(refreshed, "$.accessToken");

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("제주여행자"));

        // 5) 같은 구글 계정으로 다시 로그인하면 USER
        mockMvc.perform(post("/api/v1/auth/oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"auth-code","provider":"google"}
                                """))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void 회원가입은_한번만_가능() throws Exception {
        final String accessToken = JsonPath.read(googleLogin(), "$.accessToken");
        final String body = """
                {"nickname":"중복가입","gender":"MALE"}
                """;

        mockMvc.perform(post("/api/v1/user").header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/user").header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXIST"));
    }

    @Test
    void 지원하지_않는_provider() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"auth-code","provider":"naver"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PROVIDER"));
    }

    @Test
    void 리프레시_토큰은_액세스_토큰으로_사용불가() throws Exception {
        final String loginResponse = googleLogin();
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
