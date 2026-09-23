package com.seohamin.hondi.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChatRequestDto {

    @NotBlank
    @Size(max = 1000)
    private String message;
}
