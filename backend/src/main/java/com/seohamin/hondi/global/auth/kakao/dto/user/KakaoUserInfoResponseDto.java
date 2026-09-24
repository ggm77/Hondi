package com.seohamin.hondi.global.auth.kakao.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * 카카오 유저 정보 조회 API (/v2/user/me) 응답
 * 동의 항목에 따라 kakao_account 안의 값은 null일 수 있음
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponseDto {
    private Long id;
    private KakaoAccountDto kakao_account;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccountDto {
        private String email;
        private KakaoProfileDto profile;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfileDto {
        private String nickname;
        private String profile_image_url;
    }
}
