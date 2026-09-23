package com.seohamin.hondi.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 정보가 잘못되어 있습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 토큰입니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 Enum입니다."),
    EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "이메일이 이미 존재합니다."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
