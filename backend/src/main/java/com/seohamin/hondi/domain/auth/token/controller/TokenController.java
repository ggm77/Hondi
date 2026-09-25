package com.seohamin.hondi.domain.auth.token.controller;

import com.seohamin.hondi.domain.auth.token.dto.RefreshTokenRequestDto;
import com.seohamin.hondi.domain.auth.token.dto.TokenResponseDto;
import com.seohamin.hondi.domain.auth.token.service.TokenService;
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
public class TokenController {

    private final TokenService tokenService;

    //토큰 리프레시하는 API
    @PostMapping("/auth/token/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(
            @Validated @RequestBody final RefreshTokenRequestDto refreshTokenRequestDto
    ){
        return ResponseEntity.ok(tokenService.refreshToken(refreshTokenRequestDto));
    }
}
