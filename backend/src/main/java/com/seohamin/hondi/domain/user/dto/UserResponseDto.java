package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final String name;
    private final Role role;
    private final Instant createdAt;

    @Builder
    private UserResponseDto(
            final Long id,
            final String nickname,
            final String profileImage,
            final String name,
            final Role role,
            final Instant createdAt
    ) {
        this.id = id;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UserResponseDto(final User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.name = user.getName();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }

    //개인정보 삭제하기 위한 메서드
    public UserResponseDto removeSensitiveData(){
        return UserResponseDto.builder()
                .id(this.id)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .name(null)
                .role(this.role)
                .createdAt(this.createdAt)
                .build();
    }
}
