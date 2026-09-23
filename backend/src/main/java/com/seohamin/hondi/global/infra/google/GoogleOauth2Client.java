package com.seohamin.hondi.global.infra.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 구글 OAuth2 서버와 통신하는 클라이언트
 */
@Component
public class GoogleOauth2Client {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

    @Value("${google.web.client-id}")
    private String WEB_CLIENT_ID;

    @Value("${google.web.client-secret}")
    private String WEB_CLIENT_SECRET;

    @Value("${google.web.redirect-uri}")
    private String REDIRECT_URI;

    /**
     * 프론트에서 받은 authorization code로 구글 유저 정보를 가져오는 메서드
     * @param code 프론트에서 받은 code
     * @return 구글 유저 정보가 담긴 DTO
     */
    public UserOauth2AccountsRequestDto fetchUser(final String code){

        // 1) code를 통해 구글에서 토큰 발급
        final GoogleTokenResponse response;
        try {
            response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    TOKEN_URI,
                    WEB_CLIENT_ID,
                    WEB_CLIENT_SECRET,
                    code,
                    REDIRECT_URI
            ).execute();
        } catch (IOException ex) {
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR, ex);
        }

        // 2) 받아온 idToken에서 payload 추출
        // 구글 토큰 엔드포인트에서 TLS로 직접 받은 토큰이라 서명 검증은 생략
        final GoogleIdToken.Payload payload;
        try {
            final GoogleIdToken idToken = response.parseIdToken();
            if(idToken == null) {
                throw new CustomException(ExceptionCode.INVALID_TOKEN);
            }
            payload = idToken.getPayload();
        } catch (IOException ex){
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR, ex);
        }

        // 3) payload에서 정보 추출해서 DTO에 담기
        return UserOauth2AccountsRequestDto.builder()
                .provider("google")
                .providerUserId(payload.getSubject())
                .email(payload.getEmail())
                .name((String) payload.get("name"))
                .profileImage((String) payload.get("picture"))
                .refreshToken(response.getRefreshToken())
                .build();
    }
}
