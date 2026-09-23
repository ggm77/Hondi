package com.seohamin.hondi.domain.auth.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Oauth2RequestDto {

    @NotBlank
    private String code;

    //google
    @NotBlank
    private String provider;
}
