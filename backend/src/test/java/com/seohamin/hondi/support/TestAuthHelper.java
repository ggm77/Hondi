package com.seohamin.hondi.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 테스트에서 회원가입 후 액세스 토큰을 얻기 위한 헬퍼
 */
public final class TestAuthHelper {

    private TestAuthHelper() {}

    public static String signup(
            final MockMvc mockMvc,
            final String email,
            final String nickname,
            final String gender
    ) throws Exception {
        final String body = """
                {"email":"%s","password":"password123","nickname":"%s","gender":"%s"}
                """.formatted(email, nickname, gender);

        final String response = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }

    public static String bearer(final String accessToken) {
        return "Bearer " + accessToken;
    }
}
