package com.seohamin.hondi.global.stomp.auth;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.stomp.constants.StompConstants;
import com.seohamin.hondi.global.stomp.dto.StompAuthResultDto;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StompSessionContext {

    /**
     * 유저의 정보를 세션에 저장하는 메서드
     * @param accessor 저장을 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     * @param stompAuthResultDto 저장할 정보가 담긴 DTO
     */
    public void store(
            final StompHeaderAccessor accessor,
            final StompAuthResultDto stompAuthResultDto
    ) {
        // 1) 유저 Principal 지정
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                stompAuthResultDto.getUserIdStr(),
                null,
                stompAuthResultDto.getAuthorities()
        ));

        // 2) 복원용 attributes 설정
        final Map<String, Object> attributes = getSessionAttributes(accessor);
        attributes.put(StompConstants.ATTR_USER_ID, stompAuthResultDto.getUserIdStr()); //유저 ID 저장
        attributes.put(StompConstants.ATTR_AUTHORITIES, stompAuthResultDto.getAuthorities()); //유저 authorities 저장
        attributes.put(StompConstants.ATTR_EXP, stompAuthResultDto.getExp()); //세션 만료시간 지정 (jwt 남은 유효 시간과 같음)
    }

    /**
     * Attributes에 존재하는 정보를 바탕으로 Principal을 복원하는 메서드
     * @param accessor 복원을 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     */
    @SuppressWarnings("unchecked")
    public void restorePrincipalFromAttributes(final StompHeaderAccessor accessor) {

        // 1) attributes에서 정보 추출
        final Map<String, Object> attributes = getSessionAttributes(accessor);
        final Object userIdObj = attributes.get(StompConstants.ATTR_USER_ID);
        final Object authoritiesObj = attributes.get(StompConstants.ATTR_AUTHORITIES);

        // 2) 인증 안된 세션이면 예외
        if(!(userIdObj instanceof String userIdStr) || !(authoritiesObj instanceof List<?> authorities)){
            throw new CustomException(ExceptionCode.UNAUTHORIZED);
        }

        // 3) Principal 설정
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                userIdStr,
                null,
                (List<SimpleGrantedAuthority>) authorities
        ));
    }

    /**
     * 세션이 만료 되지 않았음을 보장하는 메서드
     * @param accessor 검사를 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     */
    public void assertNotExpired(final StompHeaderAccessor accessor) {

        final Object expObj = getSessionAttributes(accessor).get(StompConstants.ATTR_EXP);

        if(!(expObj instanceof Long exp) || System.currentTimeMillis() > exp){
            throw new CustomException(ExceptionCode.UNAUTHORIZED);
        }
    }

    private Map<String, Object> getSessionAttributes(final StompHeaderAccessor accessor) {
        final Map<String, Object> attributes = accessor.getSessionAttributes();
        if(attributes == null){
            throw new CustomException(ExceptionCode.UNAUTHORIZED);
        }
        return attributes;
    }
}
