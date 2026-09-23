package com.seohamin.hondi.domain.user.controller;

import com.seohamin.hondi.domain.user.dto.UserRequestDto;
import com.seohamin.hondi.domain.user.dto.UserResponseDto;
import com.seohamin.hondi.domain.user.service.UserService;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //내 정보 조회 API
    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDto> getMe(
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(userService.getUser(userId, userId));
    }

    //유저 조회 API
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable("id") final Long targetUserId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(userService.getUser(targetUserId, userId));
    }

    //유저 정보 수정 API
    @PatchMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final UserRequestDto userRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(userId, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        return ResponseEntity.ok(userService.updateUser(userId, userRequestDto));
    }
}
