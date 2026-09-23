package com.seohamin.hondi.domain.auth.token.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {

    @NotBlank
    private String refreshToken;
}
