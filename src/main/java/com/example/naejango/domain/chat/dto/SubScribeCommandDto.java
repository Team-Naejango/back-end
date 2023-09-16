package com.example.naejango.domain.chat.dto;

import lombok.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SubScribeCommandDto {
    private Long userId;
    private String sessionId;
    private String subscriptionId;
    private Long channelId;
    private String destination;

    public SubScribeCommandDto(Long userId, SimpMessageHeaderAccessor accessor, Long channelId) {
        this.userId = userId;
        this.sessionId = accessor.getSessionId();
        this.subscriptionId = accessor.getSubscriptionId();
        this.destination = accessor.getDestination();
        this.channelId = channelId;
    }
}
