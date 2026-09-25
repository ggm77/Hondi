package com.seohamin.hondi.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.exception.response.ExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    // 커스텀으로 만든 예외들 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleCustomException(final CustomException ex) {

        if (ex.getCause() != null) {
            log.error("EXCEPTION CODE {}, message: {}", ex.getExceptionCode().name(), ex.getMessage(), ex.getCause());
        }

        final ExceptionResponse response = new ExceptionResponse(ex.getExceptionCode());

        return ResponseEntity.status(ex.getExceptionCode().getHttpStatus()).body(response);
    }

    // @Valid 검증 실패시 발생하는 예외 처리
    // ResponseEntityExceptionHandler에 이미 존재해서 override함
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request
    ) {
        final ExceptionResponse response = new ExceptionResponse(ExceptionCode.INVALID_REQUEST);

        return ResponseEntity.status(ExceptionCode.INVALID_REQUEST.getHttpStatus()).body(response);
    }

    // 나머지 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAllException(final Exception ex) {
        log.error("INTERNAL SERVER ERROR: {}", ex.getMessage(), ex);

        final ExceptionResponse response = new ExceptionResponse(ExceptionCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity.internalServerError().body(response);
    }
}
