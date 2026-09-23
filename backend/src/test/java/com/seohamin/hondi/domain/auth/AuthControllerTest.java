package com.seohamin.hondi.domain.auth;

import com.jayway.jsonpath.JsonPath;
import com.seohamin.hondi.support.TestAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 회원가입_후_내정보_조회() throws Exception {
        final String token = TestAuthHelper.signup(mockMvc, "a@test.com", "여행자A", "FEMALE");

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", TestAuthHelper.bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("여행자A"))
                .andExpect(jsonPath("$.role").value("NOT_VERIFIED"));
    }

    @Test
    void 이메일_중복_가입_실패() throws Exception {
        TestAuthHelper.signup(mockMvc, "dup@test.com", "닉네임1", "MALE");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"dup@test.com","password":"password123","nickname":"닉네임2","gender":"MALE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_DUPLICATE"));
    }

    @Test
    void 로그인_성공과_비밀번호_불일치() throws Exception {
        TestAuthHelper.signup(mockMvc, "login@test.com", "로그인유저", "MALE");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@test.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@test.com","password":"wrong-password"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    void 리프레시_토큰은_액세스_토큰으로_사용불가() throws Exception {
        final String response = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"r@test.com","password":"password123","nickname":"리프레시","gender":"MALE"}
                                """))
                .andReturn().getResponse().getContentAsString();
        final String refreshToken = JsonPath.read(response, "$.refreshToken");
        final String accessToken = JsonPath.read(response, "$.accessToken");

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", TestAuthHelper.bearer(refreshToken)))
                .andExpect(status().isUnauthorized());

        //액세스 토큰으로 재발급 요청하면 거절
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }
}
