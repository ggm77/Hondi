package com.seohamin.hondi.global.auth.jwt;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT를 생성하고 검증한다.
 * 시크릿키와 만료 시간은 application.yml에서 가져옴
 */
@Component
public class JwtProvider {

    private static final String AUTHORITIES_CLAIM = "authorities";

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtProvider(
            @Value("${jwt.secret}") final String secret,
            @Value("${jwt.access-token.expr-time}") final long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token.expr-time}") final long refreshTokenExpirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String getTokenType() {
        return "Bearer";
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    /**
     * 액세스 토큰을 생성하는 메서드
     * @param userId 유저의 고유 아이디 번호
     * @param role 유저 권한 (ex. ROLE_USER)
     * @return JWT
     */
    public String createAccessToken(final Long userId, final String role) {
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(accessTokenExpirationSeconds); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userId.toString())
                .claim(AUTHORITIES_CLAIM, List.of(role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성하는 메서드
     * @param userId 유저의 고유 아이디 번호
     * @return JWT
     */
    public String createRefreshToken(final Long userId) {
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(refreshTokenExpirationSeconds); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT를 검증하고 claims를 얻어오는 메서드
     * 검증 실패시 CustomException(INVALID_TOKEN) 던짐
     * @param jwt JWT
     * @return JWT의 Claims
     */
    public Claims getClaims(final String jwt) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (final JwtException | IllegalArgumentException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT에서 Authorities를 얻는 메서드
     * 리프레시 토큰처럼 권한이 없는 토큰이면 빈 리스트 리턴
     * @param claims JWT의 claims
     * @return JWT의 Authorities
     */
    public List<SimpleGrantedAuthority> getAuthorities(final Claims claims) {
        final List<?> roles = claims.get(AUTHORITIES_CLAIM, List.class);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .toList();
    }
}
