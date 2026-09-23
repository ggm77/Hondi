package com.seohamin.hondi.domain.user.dto;

import com.seohamin.hondi.domain.user.entity.Gender;
import com.seohamin.hondi.domain.user.entity.User;
import lombok.Getter;

/**
 * 게시글, 채팅 등에 같이 보여줄 간단한 유저 정보
 */
@Getter
public class UserSimpleResponseDto {

    private final Long id;
    private final String nickname;
    private final Gender gender;
    private final Integer totalScore;
    private final Integer totalReviews;

    public UserSimpleResponseDto(final User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
    }
}
