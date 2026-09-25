package com.seohamin.hondi.domain.user.dto.oauth;

import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.domain.user.entity.oauth.UserOauth2Accounts;
import lombok.Getter;

@Getter
public class UserOauth2AccountsResponseDto {
    private final String provider;
    private final String email;
    private final Long userId;
    private final Role userRole;

    public UserOauth2AccountsResponseDto(final UserOauth2Accounts userOauth2Accounts){
        this.provider = userOauth2Accounts.getProvider();
        this.email = userOauth2Accounts.getEmail();
        this.userId = userOauth2Accounts.getUser().getId();
        this.userRole = userOauth2Accounts.getUser().getRole();
    }
}
