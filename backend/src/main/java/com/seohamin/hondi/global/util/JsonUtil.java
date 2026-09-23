package com.seohamin.hondi.global.util;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON 문자열로 변환하는 메서드
     * @param object 변환할 객체
     * @return JSON 문자열
     */
    public String toJson(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JacksonException ex) {
            throw new CustomException(ExceptionCode.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
