package com.seohamin.hondi.domain.auth.oauth2.service;

import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsResponseDto;
import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.domain.user.service.oauth.UserOauth2Service;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.infra.google.GoogleOauth2Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Oauth2Service {

    private final GoogleOauth2Client googleOauth2Client;
    private final UserOauth2Service userOauth2Service;
    private final JwtProvider jwtProvider;

    /**
     * OAuth2를 진행하는 메서드
     * 프론트에서 code를 받아와서 OAuth2를 진행한다.
     * 인증이 완료되면 JWT를 발급한다.
     * @param oauth2RequestDto code와 provider가 담긴 DTO
     * @return JWT와 유저 Role
     */
    public Oauth2ResponseDto processOauth2(final Oauth2RequestDto oauth2RequestDto) {

        // 1) provider에 따라서 유저 정보 가져오기
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto;
        if("google".equalsIgnoreCase(oauth2RequestDto.getProvider())) {
            userOauth2AccountsRequestDto = googleOauth2Client.fetchUser(oauth2RequestDto.getCode());
        } else {
            throw new CustomException(ExceptionCode.INVALID_PROVIDER);
        }

        // 2) 신규 유저면 임시 가입, 기존 유저면 조회
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto =
                userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        // 3) 유저 아이디와 role로 JWT만들기
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
