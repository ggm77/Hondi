package com.seohamin.hondi.domain.auth.oauth2.controller;

import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.seohamin.hondi.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.seohamin.hondi.domain.auth.oauth2.service.Oauth2Service;
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
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

    //카카오 OAuth API, 프론트에서 카카오 로그인 후 받은 access token으로 로그인
    @PostMapping("/auth/oauth2/kakao")
    public ResponseEntity<Oauth2ResponseDto> kakaoOauth2(
            @Validated @RequestBody final Oauth2RequestDto oauth2RequestDto
    ){
        return ResponseEntity.ok(oauth2Service.processKakaoOauth(oauth2RequestDto));
    }
}
