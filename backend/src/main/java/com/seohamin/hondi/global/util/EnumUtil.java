package com.seohamin.hondi.global.util;

import java.util.Optional;

public final class EnumUtil {

    // 인스턴스화 방지
    private EnumUtil() {}

    /**
     * String으로 들어온 Enum 값을 특정 Enum 타입으로 변환하는 메서드
     * @param enumClass YOUR_ENUM.class
     * @param value 변환할 문자열
     * @return 변환된 Enum 값, 변환 실패시 Optional.empty()
     * @param <T> Enum 타입
     */
    public static <T extends Enum<T>> Optional<T> toEnum(final Class<T> enumClass, final String value) {
        if (enumClass == null || value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Enum.valueOf(enumClass, value));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
