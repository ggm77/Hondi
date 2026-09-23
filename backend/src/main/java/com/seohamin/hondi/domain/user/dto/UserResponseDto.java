package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.domain.user.entity.Role;
import com.seohamin.hondi.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final String name;
    private final Gender gender;
    private final Role role;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final LocalDateTime createdAt;

    @Builder
    private UserResponseDto(
            final Long id,
            final String nickname,
            final String profileImage,
            final String name,
            final Gender gender,
            final Role role,
            final Integer totalScore,
            final Integer totalReviews,
            final LocalDateTime createdAt
    ) {
        this.id = id;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.name = name;
        this.gender = gender;
        this.role = role;
        this.totalScore = totalScore;
        this.totalReviews = totalReviews;
        this.createdAt = createdAt;
    }

    public UserResponseDto(final User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.name = user.getName();
        this.gender = user.getGender();
        this.role = user.getRole();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
        this.createdAt = user.getCreatedAt();
    }

    //개인정보 삭제하기 위한 메서드
    public UserResponseDto removeSensitiveData(){
        return UserResponseDto.builder()
                .id(this.id)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .name(null)
                .gender(this.gender)
                .role(this.role)
                .totalScore(this.totalScore)
                .totalReviews(this.totalReviews)
                .createdAt(this.createdAt)
                .build();
    }
}
