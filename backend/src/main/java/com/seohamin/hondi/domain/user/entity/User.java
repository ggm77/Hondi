package com.seohamin.hondi.domain.user.entity;

import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.entity.oauth.UserOauth2Accounts;
import com.seohamin.hondi.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 유저 정보를 저장하는 엔티티
 * OAuth로 처음 가입하면 NOT_REGISTERED 상태이고, 닉네임을 등록하면 USER가 됨
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
public class User extends BaseTimeEntity {

    //PK 유저 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //닉네임 (중복 허용 안됨, 회원가입 완료 전에는 null)
    @Column(length = 20, nullable = true)
    private String nickname;

    //프로필 사진 url (OAuth에서 받아옴)
    @Column(length = 2048, nullable = true)
    private String profileImage;

    //실제 이름 (OAuth에서 받아옴)
    @Column(length = 255, nullable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    //받은 후기들의 총 점수 //totalReviews로 나눠서 후기 평균 점수 계산
    @Column(nullable = false)
    private Integer totalScore;

    //받은 후기의 총 개수
    @Column(nullable = false)
    private Integer totalReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOauth2Accounts> userOauth2Accounts = new ArrayList<>();

    //oauth 회원가입용 생성자
    public User(final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto){
        this.profileImage = userOauth2AccountsRequestDto.getProfileImage();
        this.name = userOauth2AccountsRequestDto.getName();
        this.role = Role.NOT_REGISTERED;
        this.totalScore = 0;
        this.totalReviews = 0;
    }

    //닉네임 변경
    public void updateNickname(final String newNickname){
        this.nickname = newNickname;
    }

    //프로필 사진 변경
    public void updateProfileImage(final String newProfileImage){
        this.profileImage = newProfileImage;
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
