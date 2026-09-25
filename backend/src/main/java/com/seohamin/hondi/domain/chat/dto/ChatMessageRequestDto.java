package com.seohamin.hondi.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChatMessageRequestDto {

    @NotBlank
    @Size(max = 1000)
    private String content;
}
