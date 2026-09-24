package com.seohamin.hondi.global.config;

import com.seohamin.hondi.global.auth.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // CorsConfig의 corsConfigurationSource 사용
                .cors(Customizer.withDefaults())

                // JWT 사용하므로 CSRF, 폼 로그인, HTTP Basic 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 사용 X
                .securityContext(c -> c.securityContextRepository(new NullSecurityContextRepository()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // 인증 필요 없는 API
                        .requestMatchers(
                                "/ping",
                                "/ready",
                                "/api/swagger",
                                "/api/swagger-ui/**",
                                "/api/v1/auth/**"
                        ).permitAll()

                        // 어드민 API는 어드민만 사용 가능
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 회원가입 완료 전(NOT_REGISTERED) 유저도 사용 가능한 API
                        .requestMatchers(HttpMethod.POST, "/api/v1/user").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/me").authenticated()

                        // 그 외 요청은 회원가입 완료된 유저만 사용 가능
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )

                // 요청 헤더의 JWT 검증
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 안된 사용자 접근시 401, 권한 없는 사용자 접근시 403 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN))
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
