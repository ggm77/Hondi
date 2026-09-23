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
    private final String email;
    private final String nickname;
    private final Gender gender;
    private final String phoneNumber;
    private final Role role;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final LocalDateTime createdAt;

    @Builder
    private UserResponseDto(
            final Long id,
            final String email,
            final String nickname,
            final Gender gender,
            final String phoneNumber,
            final Role role,
            final Integer totalScore,
            final Integer totalReviews,
            final LocalDateTime createdAt
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.totalScore = totalScore;
        this.totalReviews = totalReviews;
        this.createdAt = createdAt;
    }

    public UserResponseDto(final User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
        this.createdAt = user.getCreatedAt();
    }

    //개인정보 삭제하기 위한 메서드
    public UserResponseDto removeSensitiveData(){
        return UserResponseDto.builder()
                .id(this.id)
                .email(null)
                .nickname(this.nickname)
                .gender(this.gender)
                .phoneNumber(null)
                .role(this.role)
                .totalScore(this.totalScore)
                .totalReviews(this.totalReviews)
                .createdAt(this.createdAt)
                .build();
    }
}
