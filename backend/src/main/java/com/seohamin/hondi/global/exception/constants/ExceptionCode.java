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
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "회원가입된 유저가 이미 존재 합니다."),
    INVALID_PAGING_PARAMETER(HttpStatus.BAD_REQUEST, "페이지네이션 파라미터가 잘못되어있습니다."),
    OUT_OF_SERVICE_AREA(HttpStatus.BAD_REQUEST, "서비스 지역(제주도)을 벗어난 위치입니다."),
    RIDE_NOT_EXIST(HttpStatus.BAD_REQUEST, "모집글이 존재하지 않습니다."),
    RIDE_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모집 중인 글이 아닙니다."),
    RIDE_FULL(HttpStatus.BAD_REQUEST, "모집 인원이 다 찼습니다."),
    RIDE_NOT_EDITABLE(HttpStatus.BAD_REQUEST, "수정하거나 취소할 수 없는 상태의 모집글입니다."),
    INVALID_CAPACITY(HttpStatus.BAD_REQUEST, "현재 인원보다 적게 최대 인원을 설정할 수 없습니다."),
    CANNOT_JOIN_OWN_RIDE(HttpStatus.BAD_REQUEST, "자신의 모집글에는 참여 신청할 수 없습니다."),
    GENDER_POLICY_MISMATCH(HttpStatus.BAD_REQUEST, "동성만 참여 가능한 모집글입니다."),
    RIDE_ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "이미 참여 신청했거나 참여 중입니다."),
    RIDE_REJECTED(HttpStatus.BAD_REQUEST, "참여가 거절된 모집글입니다."),
    PARTICIPANT_NOT_EXIST(HttpStatus.BAD_REQUEST, "참여 신청 정보가 존재하지 않습니다."),
    INVALID_PARTICIPANT_STATUS(HttpStatus.BAD_REQUEST, "처리할 수 없는 참여 상태입니다."),
    CHAT_ROOM_NOT_EXIST(HttpStatus.BAD_REQUEST, "채팅방이 존재하지 않습니다."),
    CHAT_ROOM_NOT_JOINED(HttpStatus.FORBIDDEN, "채팅방에 참가하고 있지 않습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다."),
    NOT_RIDE_HOST(HttpStatus.FORBIDDEN, "모집글 작성자만 할 수 있습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생했습니다."),
    KAKAO_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오와 통신 중 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
