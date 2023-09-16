package com.example.naejango.domain.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebSocketLoungeMessageSendDto {
    private Long channelId;
    private LocalDateTime sentAt;
    private String content;
}
