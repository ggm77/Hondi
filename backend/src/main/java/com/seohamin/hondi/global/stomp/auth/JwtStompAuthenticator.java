package com.seohamin.hondi.global.stomp.auth;

import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.stomp.constants.StompConstants;
import com.seohamin.hondi.global.stomp.dto.StompAuthResultDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtStompAuthenticator {

    //채팅은 회원가입 완료된 유저만 가능
    private static final Set<String> ALLOWED_ROLES = Set.of(Role.USER.getKey(), Role.ADMIN.getKey());

    private final JwtProvider jwtProvider;

    /**
     * 들어온 STOMP 메세지를 바탕으로 JWT 검사를 하는 메서드
     * @param accessor 처리할 STOMP 메세지의 헤더 정보를 포함하는 객체
     * @return AuthResultDto에 담긴 유저 정보
     */
    public StompAuthResultDto authenticateFromAccessor(final StompHeaderAccessor accessor) {
        // 1) 토큰 추출
        final String token = accessor.getFirstNativeHeader(StompConstants.AUTH_HEADER);

        // 2) Bearer로 시작하는지 검사
        if (token == null || !token.startsWith(StompConstants.PREFIX_BEARER)) {
            throw new CustomException(ExceptionCode.UNAUTHORIZED);
        }

        // 3) 토큰 검증 및 유저 ID, 유저 Authorities 추출
        final String jwt = token.substring(StompConstants.PREFIX_BEARER.length());
        final Claims claims = jwtProvider.getClaims(jwt);
        final List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthorities(claims);

        // 4) 회원가입 완료된 유저인지 확인 (리프레시 토큰도 여기서 걸러짐)
        final boolean isAllowed = authorities.stream()
                .anyMatch(authority -> ALLOWED_ROLES.contains(authority.getAuthority()));
        if (!isAllowed) {
            throw new CustomException(ExceptionCode.ACCESS_DENIED);
        }

        // 5) AuthResultDto로 리턴
        return StompAuthResultDto.builder()
                .userIdStr(claims.getSubject())
                .authorities(authorities)
                .exp(claims.getExpiration().getTime())
                .build();
    }
}
