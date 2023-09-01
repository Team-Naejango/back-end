package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateGroupChatDto {
    private Long channelId;
    private Long chatId;
}
