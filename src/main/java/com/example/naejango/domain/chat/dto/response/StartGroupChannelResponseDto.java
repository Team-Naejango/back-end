package com.example.naejango.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGroupChannelResponseDto {
    private Long channelId;
    private Long chatId;
    private String message;
}
