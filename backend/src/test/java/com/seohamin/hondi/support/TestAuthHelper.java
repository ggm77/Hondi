package com.seohamin.hondi.support;

import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.auth.jwt.JwtProvider;
import org.springframework.stereotype.Component;

/**
 * 테스트에서 회원가입 완료된 유저와 액세스 토큰을 만들기 위한 헬퍼
 */
@Component
public class TestAuthHelper {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public TestAuthHelper(final UserRepository userRepository, final JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    //회원가입 완료된 유저 생성
    public User createUser(final String nickname) {
        final User user = new User(UserOauth2AccountsRequestDto.builder()
                .provider("kakao")
                .providerUserId(nickname)
                .nickname(nickname)
                .build());
        return userRepository.save(user);
    }

    //유저의 Authorization 헤더 값
    public String bearer(final User user) {
        return "Bearer " + jwtProvider.createAccessToken(user.getId(), user.getRole().getKey());
    }
}
