package com.example.naejango.domain.chat.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StartPrivateChannelResponseDto {
    private Long channelId;
    private Long chatId;
    private String message;
}
