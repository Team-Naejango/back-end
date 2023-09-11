package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChannelAndChatDto {
    private Long channelId;
    private Long chatId;
}
