package com.seohamin.hondi.domain.user.entity;

import com.seohamin.hondi.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 정보를 저장하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
public class User extends BaseTimeEntity {

    //PK 유저 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //로그인용 이메일 (중복 허용 안됨)
    @Column(length = 255, nullable = false)
    private String email;

    //암호화된 비밀번호
    @Column(length = 255, nullable = false)
    private String password;

    //닉네임 (중복 허용 안됨)
    @Column(length = 20, nullable = false)
    private String nickname;

    //동성 매칭에 사용하는 성별
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Gender gender;

    //본인 인증된 전화번호 (E.164)
    @Column(length = 15, nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    //받은 후기들의 총 점수 //totalReviews로 나눠서 후기 평균 점수 계산
    @Column(nullable = false)
    private Integer totalScore;

    //받은 후기의 총 개수
    @Column(nullable = false)
    private Integer totalReviews;

    @Builder
    public User(
            final String email,
            final String password,
            final String nickname,
            final Gender gender
    ){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.role = Role.NOT_VERIFIED;
        this.totalScore = 0;
        this.totalReviews = 0;
    }

    //닉네임 변경
    public void updateNickname(final String newNickname){
        this.nickname = newNickname;
    }

    //비밀번호 변경 (암호화된 값)
    public void updatePassword(final String encodedPassword){
        this.password = encodedPassword;
    }

    //전화번호 변경
    public void updatePhoneNumber(final String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    //role을 일반 유저로 변경
    public void updateRoleToUser(){
        this.role = Role.USER;
    }

    //받은 후기 점수 반영
    public void addReviewScore(final int score){
        this.totalScore += score;
        this.totalReviews += 1;
    }
}
