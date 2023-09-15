package com.example.naejango.domain.chat.dto.request;

import com.example.naejango.domain.chat.domain.MessageType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebSocketMessageReceiveDto {

    private MessageType messageType;
    private Long senderId;
    private Long channelId;
    private String content;

}
