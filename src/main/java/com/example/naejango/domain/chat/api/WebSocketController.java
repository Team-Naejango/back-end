package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.MessageService;
import com.example.naejango.domain.chat.dto.SendMessageRequestDto;
import com.example.naejango.domain.chat.dto.SendMessageResponseDto;
import com.example.naejango.domain.chat.dto.SubscribeResponseDto;
import com.example.naejango.domain.chat.dto.response.UnsubscribeResponseDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageService messageService;
    ConcurrentMap<Long, Set<Long>> subscribeInfoMap = new ConcurrentHashMap<>();

    /** 웹소켓 통신 중 정보를 수신하는 채널을 구독하는 Endpoint */
    @SubscribeMapping("/user/queue/info")
    public SubscribeResponseDto subscribeInfoChannel() {
        return SubscribeResponseDto.builder().message("소켓 통신 정보를 수신합니다.").build();
    }

    /** 채팅 Channel 을 구독하는 Endpoint */
    @SubscribeMapping("/topic/channel/{channelId}")
    public SubscribeResponseDto subscribeChatChannel(@DestinationVariable("channelId") Long channelId, SimpMessageHeaderAccessor accessor) {
        Long userId = getUserId(accessor);
        subscribeInfoMap.computeIfAbsent(channelId, k -> new HashSet<>()).add(userId);
        return SubscribeResponseDto.builder().userId(userId).message("채팅 채널 구독을 시작합니다.").subscribingChannelId(channelId).build();
    }

    /** 채팅 Channel 에 메세지를 보내는 Endpoint */
    @MessageMapping("/chat/channel/{channelId}")
    @SendTo("/topic/channel/{channelId}")
    public SendMessageResponseDto sendMessage(@RequestBody SendMessageRequestDto requestDto,
                                              @DestinationVariable("channelId") Long channelId,
                                              SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = getUserId(headerAccessor);
        messageService.publishMessage(channelId, senderId, requestDto.getContent(), subscribeInfoMap.get(channelId));
        return new SendMessageResponseDto(senderId, channelId, requestDto.getContent());
    }

    /**
     * 구독을 취소하는 Endpoint
     * UNSUBSCRIBE 가 아닌 MESSAGE/SEND 명령으로 HashMap 에 등록되어있는 구독 정보를 지우는 용도입니다
     */
    @MessageMapping("/unsubscribe")
    @SendToUser("/queue/info")
    public UnsubscribeResponseDto unsubscribeChannel(SimpMessageHeaderAccessor accessor) {
        Long userId = getUserId(accessor);
        Long channelId = getChannelId(accessor);

        // 구독 정보 지우기
        subscribeInfoMap.computeIfPresent(channelId, (key, set) -> {
            if(!set.remove(userId)) throw new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
            return set;
        });

        return UnsubscribeResponseDto.builder().userId(userId).unsubscribedChannelId(channelId).message("채팅 채널 구독이 취소 되었습니다.").build();
    }

    /** 웹소켓 통신 중 발생하는 예외를 핸들링합니다. */
    @MessageExceptionHandler
    @SendToUser("/queue/info")
    public WebSocketErrorResponse handleException(Throwable e) {
        if (e instanceof WebSocketException) {
            WebSocketException exception = (WebSocketException) e;
            return WebSocketErrorResponse.response(exception.getErrorCode());
        }
        e.printStackTrace();
        return WebSocketErrorResponse.builder().error(e.getMessage()).message("통신 중 에러가 발생하였습니다.").build();
    }

    private Long getUserId(SimpMessageHeaderAccessor accessor) {
        Object channelId = accessor.getHeader("userId");
        if(channelId instanceof Long) return (Long) channelId;
        if(channelId instanceof String) return Long.valueOf((String) channelId);
        return null;
    }

    private Long getChannelId(SimpMessageHeaderAccessor accessor) {
        Object channelId = accessor.getHeader("channelId");
        if(channelId instanceof Long) return (Long) channelId;
        if(channelId instanceof String) return Long.valueOf((String) channelId);
        return null;
    }

}