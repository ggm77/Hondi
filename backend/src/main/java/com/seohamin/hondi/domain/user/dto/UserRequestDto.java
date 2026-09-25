package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.global.validation.Update;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 유저 정보 수정(Update) 요청 DTO
 * 변경할 값만 보내면 됨
 */
@Getter
public class UserRequestDto {

    @Size(min = 2, max = 20, groups = Update.class)
    private String nickname;

    @Size(max = 2048, groups = Update.class)
    private String profileImage;
}
