package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.global.validation.Create;
import com.seohamin.hondi.global.validation.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 회원가입(Create), 유저 정보 수정(Update) 요청 DTO
 * 수정시에는 변경할 값만 보내면 됨
 */
@Getter
public class UserRequestDto {

    @NotBlank(groups = Create.class)
    @Size(min = 2, max = 20, groups = {Create.class, Update.class})
    private String nickname;

    //회원가입 이후 변경 불가
    @NotNull(groups = Create.class)
    private Gender gender;

    @Size(max = 2048, groups = {Create.class, Update.class})
    private String profileImage;
}
