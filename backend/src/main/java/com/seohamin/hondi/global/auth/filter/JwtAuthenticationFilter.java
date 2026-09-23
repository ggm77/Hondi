package com.seohamin.hondi.global.auth.filter;

import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import com.seohamin.hondi.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 필터에 JWT 검증 과정 추가
 * 토큰이 없거나 검증에 실패하면 익명으로 진행됨 (인증 필요한 API는 인가 단계에서 401 처리)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        //JWT를 제대로 가지고 있는지 검사, 없으면 익명으로 진행
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final Claims claims;
        try {
            claims = jwtProvider.getClaims(authorizationHeader.substring(BEARER_PREFIX.length()));
        } catch (final CustomException ex) { //검증 실패시 익명으로 진행
            filterChain.doFilter(request, response);
            return;
        }

        //이미 JWT 검증으로 인증 완료됨
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(claims.getSubject(), null, jwtProvider.getAuthorities(claims))
        );

        filterChain.doFilter(request, response);
    }
}
