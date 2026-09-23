package com.seohamin.hondi.domain.auth.oauth2.dto;

import com.seohamin.hondi.domain.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Oauth2ResponseDto {
    //NOT_REGISTERED면 프론트에서 회원가입(닉네임, 성별 입력) 화면으로 이동
    private final Role role;
    private final String accessToken;
    private final String tokenType;
    private final Long exprTime;
    private final String refreshToken;

    @Builder
    public Oauth2ResponseDto(
            final Role role,
            final String accessToken,
            final String tokenType,
            final Long exprTime,
            final String refreshToken
    ){
        this.role = role;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.exprTime = exprTime;
        this.refreshToken = refreshToken;
    }
}
