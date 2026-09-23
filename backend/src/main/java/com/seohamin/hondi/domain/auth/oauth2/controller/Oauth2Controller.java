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

    //프론트에서 authorization code 받아서 oauth2완료하는 API
    @PostMapping("/auth/oauth2")
    public ResponseEntity<Oauth2ResponseDto> oauth2Login(
            @Validated @RequestBody final Oauth2RequestDto oauth2RequestDto
    ){
        return ResponseEntity.ok(oauth2Service.processOauth2(oauth2RequestDto));
    }
}
