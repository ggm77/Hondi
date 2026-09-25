package com.seohamin.hondi.domain.user.service;

import com.seohamin.hondi.domain.user.dto.UserRequestDto;
import com.seohamin.hondi.domain.user.dto.UserResponseDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저의 정보를 조회하는 메서드
     * 다른 회원의 정보도 조회 가능
     * 다른 회원 정보시 dto에서 민감한 정보는 빠짐
     * @param targetUserId 조회할 유저 아이디
     * @param requestUserId 요청한 유저 아이디
     * @return 유저 DTO
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUser(
            final Long targetUserId,
            final Long requestUserId
    ){
        // 1) 유저 조회
        final User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 기본 DTO생성
        final UserResponseDto userResponseDto = new UserResponseDto(user);

        // 3) 다른 유저 조회시 민감 정보 삭제
        if(!targetUserId.equals(requestUserId)){
            return userResponseDto.removeSensitiveData();
        }

        return userResponseDto;
    }

    /**
     * 유저의 정보를 수정하는 메서드
     * 본인의 정보만 수정 가능
     * 원하는 정보만 수정 가능
     * @param userId 자신의 유저 아이디
     * @param userRequestDto 수정할 정보들
     * @return 수정된 유저 DTO
     */
    @Transactional
    public UserResponseDto updateUser(
            final Long userId,
            final UserRequestDto userRequestDto
    ){
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        //변경할 닉네임이 존재하면 변경
        final String nickname = userRequestDto.getNickname();
        if(nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())){
            user.updateNickname(nickname);
        }

        //변경할 프로필 사진이 존재하면 변경
        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isBlank()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }

        return new UserResponseDto(user);
    }
}
