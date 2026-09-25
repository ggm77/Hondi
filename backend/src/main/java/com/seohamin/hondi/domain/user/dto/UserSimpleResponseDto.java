package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.domain.user.entity.User;
import lombok.Getter;

/**
 * 모집글 등에 같이 보여줄 간단한 유저 정보
 */
@Getter
public class UserSimpleResponseDto {

    private final Long id;
    private final String nickname;
    private final String profileImage;

    public UserSimpleResponseDto(final User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }
}
