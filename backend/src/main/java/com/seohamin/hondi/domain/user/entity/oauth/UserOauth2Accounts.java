package com.seohamin.hondi.domain.user.entity.oauth;

import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 유저와 연결된 OAuth 계정 정보
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_oauth2_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_provider_user", columnNames = {"provider", "provider_user_id"})
        }
)
public class UserOauth2Accounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //kakao...
    @Column(length = 20, nullable = false)
    private String provider;

    //카카오 회원번호같이 oauth에서 쓰는 식별자
    @Column(length = 255, nullable = false)
    private String providerUserId;

    @Column(length = 320, nullable = true)
    private String email;

    @Column(length = 255, nullable = true)
    private String name;

    @Column(length = 2048, nullable = true)
    private String profileImage;

    //oauth에서 제공하는 리프레시 토큰 (unlink시 필요함)
    @Column(length = 4100, nullable = true)
    private String refreshToken;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant linkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserOauth2Accounts(
            final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto,
            final User user
    ){
        this.provider = userOauth2AccountsRequestDto.getProvider();
        this.providerUserId = userOauth2AccountsRequestDto.getProviderUserId();
        this.email = userOauth2AccountsRequestDto.getEmail();
        this.name = userOauth2AccountsRequestDto.getName();
        this.profileImage = userOauth2AccountsRequestDto.getProfileImage();
        this.refreshToken = userOauth2AccountsRequestDto.getRefreshToken();
        this.user = user;
    }

    //리프레시 토큰 수정하는 메서드
    public void updateRefreshToken(final String refreshToken){
        this.refreshToken = refreshToken;
    }
}
