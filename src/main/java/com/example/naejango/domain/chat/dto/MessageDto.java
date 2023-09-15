package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.domain.MessageType;
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
    private Long channelId;
    private Long senderId;
    private MessageType messageType;
    private String content;

    public MessageDto(Message message) {
        this.messageId = message.getId();
        this.channelId = message.getChannel().getId();
        this.senderId = message.getSenderId();
        this.messageType = message.getMessageType();
        this.content = message.getContent();
    }
}
