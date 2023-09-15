package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.request.WebSocketMessageReceiveDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebSocketMessageSendDto {
    private MessageType messageType;
    private Long senderId;
    private Long channelId;
    private LocalDateTime sentAt;
    private String content;

    public WebSocketMessageSendDto(WebSocketMessageReceiveDto receiveDto) {
        this.messageType = receiveDto.getMessageType();
        this.senderId = receiveDto.getSenderId();
        this.channelId = receiveDto.getChannelId();
        this.sentAt = LocalDateTime.now();
        this.content = receiveDto.getContent();
    }
}
