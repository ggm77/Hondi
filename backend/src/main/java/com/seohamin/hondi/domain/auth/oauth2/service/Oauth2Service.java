package com.seohamin.hondi.domain.auth.oauth2.service;

import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsResponseDto;
import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.domain.user.service.oauth.UserOauth2Service;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.auth.kakao.client.KakaoAuthClient;
import com.seohamin.hondi.global.auth.kakao.dto.user.KakaoUserInfoResponseDto;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Oauth2Service {

    private final KakaoAuthClient kakaoAuthClient;
    private final UserOauth2Service userOauth2Service;
    private final JwtProvider jwtProvider;

    /**
     * 카카오 OAuth2 진행하는 메서드
     * 프론트에서 받은 카카오 access token으로 유저 정보를 조회하고 JWT를 발급한다.
     * @param oauth2RequestDto 카카오 access token 담긴 DTO
     * @return JWT와 유저 Role
     */
    public Oauth2ResponseDto processKakaoOauth(final Oauth2RequestDto oauth2RequestDto) {

        // 1) access token으로 카카오 유저 정보 조회
        final KakaoUserInfoResponseDto userInfoResponse = kakaoAuthClient.requestUserInfo(oauth2RequestDto.getAccessToken());
        if(userInfoResponse == null || userInfoResponse.getId() == null){
            throw new CustomException(ExceptionCode.KAKAO_REQUEST_ERROR);
        }

        // 2) 유저 정보에서 이메일, 닉네임, 프로필 이미지 추출 (동의 안했으면 null)
        final KakaoUserInfoResponseDto.KakaoAccountDto kakaoAccount = userInfoResponse.getKakao_account();
        final KakaoUserInfoResponseDto.KakaoProfileDto profile = kakaoAccount != null ? kakaoAccount.getProfile() : null;

        // 3) 유저 정보 담긴 DTO 생성 (access token 방식에서는 refresh token을 발급받지 않음)
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                .provider("kakao")
                .providerUserId(String.valueOf(userInfoResponse.getId()))
                .email(kakaoAccount != null ? kakaoAccount.getEmail() : null)
                .name(profile != null ? profile.getNickname() : null)
                .profileImage(profile != null ? profile.getProfile_image_url() : null)
                .build();

        // 4) 신규 유저면 임시 가입, 기존 유저면 조회
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto =
                userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        // 5) 유저 아이디와 role로 JWT만들기
        final Long userId = userOauth2AccountsResponseDto.getUserId();
        final Role userRole = userOauth2AccountsResponseDto.getUserRole();

        return Oauth2ResponseDto.builder()
                .role(userRole)
                .accessToken(jwtProvider.createAccessToken(userId, userRole.getKey()))
                .tokenType(jwtProvider.getTokenType())
                .exprTime(jwtProvider.getAccessTokenExpirationSeconds())
                .refreshToken(jwtProvider.createRefreshToken(userId))
                .build();
    }
}
