package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.domain.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MessagePublishCommandDto {
    private MessageType messageType;
    private Long senderId;
    private Long channelId;
    private String content;

    public MessagePublishCommandDto(MessageType messageType, Long senderId, Long channelId) {
        this.messageType = messageType;
        this.senderId = senderId;
        this.channelId = channelId;
        this.content = messageType.getDefaultMessage();
    }

    public Message toEntity(Channel channel) {
        return Message.builder()
                .messageType(this.messageType)
                .senderId(this.senderId)
                .channel(channel)
                .content(this.content)
                .build();
    }

    public WebSocketMessageSendDto toSendDto() {
        return WebSocketMessageSendDto.builder()
                .messageType(this.messageType)
                .senderId(this.senderId)
                .channelId(channelId)
                .content(this.content)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
