package com.seohamin.hondi.domain.ride.dto;

import com.seohamin.hondi.global.validation.Create;
import com.seohamin.hondi.global.validation.Update;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 모집글 생성(Create), 수정(Update) 요청 DTO
 * 수정은 출발 시간, 최대 인원, 메모만 가능
 */
@Getter
public class RideRequestDto {

    @NotBlank(groups = Create.class)
    @Size(max = 100, groups = Create.class)
    private String originName;

    @NotNull(groups = Create.class)
    private BigDecimal originLat;

    @NotNull(groups = Create.class)
    private BigDecimal originLon;

    @NotBlank(groups = Create.class)
    @Size(max = 100, groups = Create.class)
    private String destName;

    @NotNull(groups = Create.class)
    private BigDecimal destLat;

    @NotNull(groups = Create.class)
    private BigDecimal destLon;

    @NotNull(groups = Create.class)
    @Future(groups = {Create.class, Update.class})
    private LocalDateTime departureAt;

    //방장 포함 최대 인원 (택시 기준 2~4, 소형 승합차 고려해서 최대 6)
    @NotNull(groups = Create.class)
    @Min(value = 2, groups = {Create.class, Update.class})
    @Max(value = 6, groups = {Create.class, Update.class})
    private Integer capacity;

    @Size(max = 500, groups = {Create.class, Update.class})
    private String memo;
}
