package com.seohamin.hondi.global.auth.kakao;

import com.seohamin.hondi.global.auth.kakao.client.KakaoAuthClient;
import com.seohamin.hondi.global.auth.kakao.dto.user.KakaoUserInfoResponseDto;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 카카오 API 응답을 흉내내서 파싱과 에러 처리를 확인
 */
class KakaoAuthClientTest {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private MockRestServiceServer server;
    private KakaoAuthClient kakaoAuthClient;

    @BeforeEach
    void setUp() {
        final RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        kakaoAuthClient = new KakaoAuthClient(builder, USER_INFO_URI);
    }

    @Test
    void 유저_정보를_파싱한다() {
        //실제 응답에는 모르는 필드도 섞여 있음
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(header("Authorization", "Bearer token-123"))
                .andRespond(withSuccess("""
                        {
                          "id": 123456789,
                          "connected_at": "2026-09-24T00:00:00Z",
                          "properties": {"nickname": "홍길동"},
                          "kakao_account": {
                            "profile_nickname_needs_agreement": false,
                            "profile": {"nickname": "홍길동", "profile_image_url": "https://k.kakaocdn.net/p.jpg", "is_default_image": false},
                            "email": "traveler@kakao.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        final KakaoUserInfoResponseDto result = kakaoAuthClient.requestUserInfo("token-123");

        assertThat(result.getId()).isEqualTo(123456789L);
        assertThat(result.getKakao_account().getEmail()).isEqualTo("traveler@kakao.com");
        assertThat(result.getKakao_account().getProfile().getNickname()).isEqualTo("홍길동");
        assertThat(result.getKakao_account().getProfile().getProfile_image_url()).isEqualTo("https://k.kakaocdn.net/p.jpg");
    }

    @Test
    void 동의_안한_항목은_null() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{\"id\": 1}", MediaType.APPLICATION_JSON));

        final KakaoUserInfoResponseDto result = kakaoAuthClient.requestUserInfo("token");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getKakao_account()).isNull();
    }

    @Test
    void 잘못된_토큰이면_INVALID_TOKEN() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> kakaoAuthClient.requestUserInfo("bad"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getExceptionCode())
                .isEqualTo(ExceptionCode.INVALID_TOKEN);
    }

    @Test
    void 카카오_서버_오류면_KAKAO_REQUEST_ERROR() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withServerError());

        assertThatThrownBy(() -> kakaoAuthClient.requestUserInfo("token"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getExceptionCode())
                .isEqualTo(ExceptionCode.KAKAO_REQUEST_ERROR);
    }
}
