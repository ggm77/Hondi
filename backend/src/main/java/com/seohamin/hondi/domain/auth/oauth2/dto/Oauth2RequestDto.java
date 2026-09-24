package com.seohamin.hondi.domain.auth.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Oauth2RequestDto {

    //프론트에서 카카오 로그인 후 받은 access token
    @NotBlank
    private String accessToken;
}
