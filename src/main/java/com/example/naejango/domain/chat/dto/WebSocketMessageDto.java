package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.MessageType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebSocketMessageDto {

    private MessageType messageType;
    private Long userId;
    private Long channelId;
    private String content;

}
