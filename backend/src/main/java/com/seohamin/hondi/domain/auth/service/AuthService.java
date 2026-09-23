package com.seohamin.hondi.domain.auth.service;

import com.seohamin.hondi.domain.auth.dto.LoginRequestDto;
import com.seohamin.hondi.domain.auth.dto.RefreshTokenRequestDto;
import com.seohamin.hondi.domain.auth.dto.SignupRequestDto;
import com.seohamin.hondi.domain.auth.dto.TokenResponseDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입 후 바로 토큰을 발급하는 메서드
     * 가입 직후 role은 NOT_VERIFIED이고, SMS 인증 후 USER로 바뀜
     * @param signupRequestDto 회원가입 요청 DTO
     * @return 발급된 토큰
     */
    @Transactional
    public TokenResponseDto signup(final SignupRequestDto signupRequestDto){

        // 1) 이메일 중복 검사
        final String email = signupRequestDto.getEmail().trim().toLowerCase();
        if(userRepository.existsByEmail(email)){
            throw new CustomException(ExceptionCode.EMAIL_DUPLICATE);
        }

        // 2) 닉네임 중복 검사
        if(userRepository.existsByNickname(signupRequestDto.getNickname())){
            throw new CustomException(ExceptionCode.NICKNAME_DUPLICATE);
        }

        // 3) 유저 저장
        final User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .nickname(signupRequestDto.getNickname())
                .gender(signupRequestDto.getGender())
                .build());

        // 4) 토큰 발급
        return issueToken(user);
    }

    /**
     * 이메일, 비밀번호로 로그인하는 메서드
     * @param loginRequestDto 로그인 요청 DTO
     * @return 발급된 토큰
     */
    @Transactional(readOnly = true)
    public TokenResponseDto login(final LoginRequestDto loginRequestDto){

        final String email = loginRequestDto.getEmail().trim().toLowerCase();

        // 1) 유저 조회 (이메일 존재 여부 노출 방지를 위해 같은 에러 사용)
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.LOGIN_FAILED));

        // 2) 비밀번호 검사
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())){
            throw new CustomException(ExceptionCode.LOGIN_FAILED);
        }

        return issueToken(user);
    }

    /**
     * 리프레시 토큰으로 JWT를 재발급하는 메서드
     * @param refreshTokenRequestDto 리프레시 토큰 담겨있는 DTO
     * @return 재발급된 토큰
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

        return issueToken(user);
    }

    /**
     * 유저 정보로 액세스 토큰, 리프레시 토큰을 발급하는 메서드
     * role이 바뀐 경우(ex. SMS 인증)에도 재발급용으로 사용
     * @param user 토큰 발급할 유저
     * @return 발급된 토큰
     */
    public TokenResponseDto issueToken(final User user){
        return TokenResponseDto.builder()
                .accessToken(jwtProvider.createAccessToken(user.getId(), user.getRole().getKey()))
                .tokenType(jwtProvider.getTokenType())
                .exprTime(jwtProvider.getAccessTokenExpirationSeconds())
                .refreshToken(jwtProvider.createRefreshToken(user.getId()))
                .build();
    }
}
