package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private Long messageId;
    private Long senderId;

    public MessageDto(Message message) {
        this.messageId = message.getId();
        this.senderId = message.getSenderId();
    }
}
