package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Message;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SendMessageResponseDto {
    private Long senderId;
    private String content;

    public SendMessageResponseDto(Message message) {
        this.senderId = message.getSenderId();
        this.content = message.getContent();
    }
}
