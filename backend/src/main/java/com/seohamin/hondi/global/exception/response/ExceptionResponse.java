package com.seohamin.hondi.global.exception.response;

import lombok.Getter;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public class ExceptionResponse {
    private final Instant timestamp;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ExceptionResponse(final ExceptionCode exceptionCode) {
        this.timestamp = Instant.now();
        this.httpStatus = exceptionCode.getHttpStatus();
        this.code = exceptionCode.name();
        this.message = exceptionCode.getMessage();
    }
}
