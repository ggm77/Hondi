package com.seohamin.hondi.global.health;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final EntityManager entityManager;

    // 서버 동작 여부 확인
    @GetMapping("/ping")
    public String ping(){
        return "pong";
    }

    // DB 연결까지 확인
    @GetMapping("/ready")
    public String ready(){
        try{
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return "ready";
        } catch (Exception ex){
            throw new CustomException(ExceptionCode.SERVICE_UNAVAILABLE, ex);
        }
    }
}
