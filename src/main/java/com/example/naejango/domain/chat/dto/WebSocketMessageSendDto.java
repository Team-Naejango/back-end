package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
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
}
