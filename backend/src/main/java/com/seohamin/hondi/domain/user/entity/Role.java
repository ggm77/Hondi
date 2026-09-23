package com.seohamin.hondi.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    NOT_VERIFIED("ROLE_NOT_VERIFIED", "본인 인증이 되지 않은 유저"),
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
