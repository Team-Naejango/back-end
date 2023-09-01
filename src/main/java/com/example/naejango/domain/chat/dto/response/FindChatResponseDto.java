package com.example.naejango.domain.chat.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FindChatResponseDto {
    private Long chatId;
    private String message;
}
