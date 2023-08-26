package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.MessageService;
import com.example.naejango.domain.chat.dto.SendMessageRequestDto;
import com.example.naejango.domain.chat.dto.SendMessageResponseDto;
import com.example.naejango.domain.chat.dto.SubscribeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageService messageService;
    ConcurrentMap<Long, Set<Long>> topicSubscribers = new ConcurrentHashMap<>();

    /**
     * SendTo 로 맵핑된 Channel 에 메세지를 보내는 api
     */
    @MessageMapping("/chat/channel/{channelId}")
    @SendTo("/topic/channel/{channelId}")
    public SendMessageResponseDto sendMessage(@RequestBody SendMessageRequestDto requestDto,
                                              @DestinationVariable("channelId") Long channelId,
                                              SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = Long.valueOf((String) Objects.requireNonNull(headerAccessor.getHeader("userId")));
        messageService.publishMessage(channelId, senderId, requestDto.getContent(), topicSubscribers.get(channelId));
        return new SendMessageResponseDto(senderId, channelId, requestDto.getContent());
    }

    /**
     * 특정 Channel 을 구독하는 api
     * 구독 요청시 한번만 동작하는 로직입니다.
     */
    @SubscribeMapping("/topic/channel/{channelId}")
    public SubscribeResponseDto subscribeChannel(@DestinationVariable("channelId") Long channelId, SimpMessageHeaderAccessor headerAccessor) {
        Long requestUserId = Long.valueOf((String) Objects.requireNonNull(headerAccessor.getHeader("userId")));
        topicSubscribers.computeIfAbsent(channelId, k -> new HashSet<>()).add(requestUserId);
        return SubscribeResponseDto.builder().userId(requestUserId).message("채팅 채널 구독을 시작합니다.").channelId(channelId).build();
    }


}