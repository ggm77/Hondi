package com.seohamin.hondi.domain.user.service.oauth;

import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsResponseDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.entity.oauth.UserOauth2Accounts;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.domain.user.repository.oauth.UserOauth2AccountsRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOauth2Service {

    private final UserRepository userRepository;
    private final UserOauth2AccountsRepository userOauth2AccountsRepository;

    /**
     * OAuth로 로그인 할 때 가입 안된 유저면 카카오 닉네임으로 바로 가입시키는 메서드
     * @param userOauth2AccountsRequestDto OAuth에서 받아온 유저 정보
     * @return 연결된 유저 ID와 Role
     */
    @Transactional
    public UserOauth2AccountsResponseDto upsertOAuthUser(final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto) {

        // 1) provider, provider user id 입력값 null 검사
        if(userOauth2AccountsRequestDto.getProvider() == null || userOauth2AccountsRequestDto.getProvider().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }
        if(userOauth2AccountsRequestDto.getProviderUserId() == null || userOauth2AccountsRequestDto.getProviderUserId().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) oauth로 이미 가입했는지 확인
        final Optional<UserOauth2Accounts> userOauth2Accounts = userOauth2AccountsRepository.findByProviderAndProviderUserId(
                userOauth2AccountsRequestDto.getProvider(),
                userOauth2AccountsRequestDto.getProviderUserId()
        );

        // 3) 이미 가입했다면 리프레시 토큰만 갱신
        if(userOauth2Accounts.isPresent()){
            final String refreshToken = userOauth2AccountsRequestDto.getRefreshToken();
            if(refreshToken != null && !refreshToken.isEmpty()){
                userOauth2Accounts.get().updateRefreshToken(refreshToken);
            }

            return new UserOauth2AccountsResponseDto(userOauth2Accounts.get());
        }

        // 4) 가입 안되어있으므로 카카오 닉네임으로 바로 가입
        final User savedUser = userRepository.save(new User(userOauth2AccountsRequestDto));
        final UserOauth2Accounts savedUserOauth2Accounts = userOauth2AccountsRepository.save(
                new UserOauth2Accounts(userOauth2AccountsRequestDto, savedUser)
        );

        //리프레시 토큰 없으면 로그 찍기
        if(userOauth2AccountsRequestDto.getRefreshToken() == null || userOauth2AccountsRequestDto.getRefreshToken().isEmpty()) {
            log.warn(
                    "OAuth2 refresh token missing. provider={}, sub={}, userId={}",
                    userOauth2AccountsRequestDto.getProvider(),
                    userOauth2AccountsRequestDto.getProviderUserId(),
                    savedUser.getId()
            );
        }

        return new UserOauth2AccountsResponseDto(savedUserOauth2Accounts);
    }
}
