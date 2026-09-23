package com.seohamin.hondi.domain.auth.controller;

import com.seohamin.hondi.domain.auth.dto.LoginRequestDto;
import com.seohamin.hondi.domain.auth.dto.RefreshTokenRequestDto;
import com.seohamin.hondi.domain.auth.dto.SignupRequestDto;
import com.seohamin.hondi.domain.auth.dto.TokenResponseDto;
import com.seohamin.hondi.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //회원가입 API
    @PostMapping("/auth/signup")
    public ResponseEntity<TokenResponseDto> signup(
            @Validated @RequestBody final SignupRequestDto signupRequestDto
    ){
        return ResponseEntity.ok(authService.signup(signupRequestDto));
    }

    //로그인 API
    @PostMapping("/auth/login")
    public ResponseEntity<TokenResponseDto> login(
            @Validated @RequestBody final LoginRequestDto loginRequestDto
    ){
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    //토큰 리프레시하는 API
    @PostMapping("/auth/token/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(
            @Validated @RequestBody final RefreshTokenRequestDto refreshTokenRequestDto
    ){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequestDto));
    }
}
