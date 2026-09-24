package com.seohamin.hondi.domain.user.service;

import com.seohamin.hondi.domain.user.dto.UserRequestDto;
import com.seohamin.hondi.domain.user.dto.UserResponseDto;
import com.seohamin.hondi.domain.user.entity.Role;
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
     * OAuth로 임시 가입된 유저의 회원가입을 완료하는 메서드
     * 닉네임을 등록하고 role을 USER로 변경
     * 변경된 role을 반영하려면 프론트에서 토큰 재발급을 해야 함
     * @param userRequestDto 회원가입 요청 DTO
     * @param userId oauth에서 등록된 유저 아이디
     * @return 등록된 유저 정보 DTO
     */
    @Transactional
    public UserResponseDto createUser(
            final UserRequestDto userRequestDto,
            final Long userId
    ){
        // 1) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 이미 회원가입 완료된 유저인지 확인
        if(user.getRole() != Role.NOT_REGISTERED){
            throw new CustomException(ExceptionCode.USER_ALREADY_EXIST);
        }

        // 3) 닉네임 중복 검사
        if(userRepository.existsByNickname(userRequestDto.getNickname())){
            throw new CustomException(ExceptionCode.NICKNAME_DUPLICATE);
        }

        // 4) 정보 등록
        user.updateNickname(userRequestDto.getNickname());
        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isBlank()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }
        user.updateRoleToUser();

        return new UserResponseDto(user);
    }

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

            //닉네임 중복 검사
            if(userRepository.existsByNickname(nickname)){
                throw new CustomException(ExceptionCode.NICKNAME_DUPLICATE);
            }

            user.updateNickname(nickname);
        }

        //변경할 프로필 사진이 존재하면 변경
        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isBlank()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }

        return new UserResponseDto(user);
    }
}
