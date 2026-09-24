package com.seohamin.hondi.global.auth.kakao.client;

import com.seohamin.hondi.global.auth.kakao.dto.user.KakaoUserInfoResponseDto;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 카카오 API 서버와 통신하는 클라이언트
 */
@Component
@Slf4j
public class KakaoAuthClient {

    private final RestClient restClient;
    private final String userInfoUri;

    @Autowired
    public KakaoAuthClient(
            @Value("${kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}") final String userInfoUri
    ) {
        this(RestClient.builder(), userInfoUri);
    }

    //테스트에서 목 서버를 붙이기 위한 생성자
    public KakaoAuthClient(
            final RestClient.Builder restClientBuilder,
            final String userInfoUri
    ) {
        this.restClient = restClientBuilder.build();
        this.userInfoUri = userInfoUri;
    }

    /**
     * 카카오 access token으로 유저 정보를 받아오는 메서드
     * 토큰이 잘못되었으면 INVALID_TOKEN, 그 외 실패는 KAKAO_REQUEST_ERROR
     * @param accessToken 프론트에서 카카오 로그인 후 받은 access token
     * @return 카카오 유저 정보
     */
    public KakaoUserInfoResponseDto requestUserInfo(final String accessToken) {
        try {
            return restClient.get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Kakao User Info API client error: {}", response.getStatusCode());
                        throw new CustomException(ExceptionCode.INVALID_TOKEN);
                    })
                    .body(KakaoUserInfoResponseDto.class);
        } catch (final RestClientException ex) {
            throw new CustomException(ExceptionCode.KAKAO_REQUEST_ERROR, ex);
        }
    }
}
