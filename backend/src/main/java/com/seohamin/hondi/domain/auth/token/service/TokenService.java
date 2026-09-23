package com.seohamin.hondi.domain.auth.token.service;

import com.seohamin.hondi.domain.auth.token.dto.RefreshTokenRequestDto;
import com.seohamin.hondi.domain.auth.token.dto.TokenResponseDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    /**
     * 리프레시 토큰으로 JWT를 재발급하는 메서드
     * 회원가입 완료 후 바뀐 role을 토큰에 반영할 때도 사용
     * @param refreshTokenRequestDto 리프레시 토큰 담겨있는 DTO
     * @return TokenResponseDto에 담긴 토큰
     */
    @Transactional(readOnly = true)
    public TokenResponseDto refreshToken(final RefreshTokenRequestDto refreshTokenRequestDto){

        // 1) 토큰 검증
        final Claims claims = jwtProvider.getClaims(refreshTokenRequestDto.getRefreshToken());

        // 2) 권한 정보가 있으면 액세스 토큰이므로 거절
        if(!jwtProvider.getAuthorities(claims).isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }

        // 3) 유저 조회
        final Long userId = Long.parseLong(claims.getSubject());
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 토큰 재발급
        return TokenResponseDto.builder()
                .accessToken(jwtProvider.createAccessToken(userId, user.getRole().getKey()))
                .tokenType(jwtProvider.getTokenType())
                .exprTime(jwtProvider.getAccessTokenExpirationSeconds())
                .refreshToken(jwtProvider.createRefreshToken(userId))
                .build();
    }
}
