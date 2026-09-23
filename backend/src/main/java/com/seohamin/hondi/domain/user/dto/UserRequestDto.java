package com.seohamin.hondi.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 유저 정보 수정 요청 DTO
 * 변경할 값만 보내면 됨
 */
@Getter
public class UserRequestDto {

    @Size(min = 2, max = 20)
    private String nickname;

    @Size(min = 8, max = 64)
    private String password;
}
