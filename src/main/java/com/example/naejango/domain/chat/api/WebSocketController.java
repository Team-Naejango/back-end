package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.application.websocket.SubscribeService;
import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.WebSocketMessageSendDto;
import com.example.naejango.domain.chat.dto.request.WebSocketMessageReceiveDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.example.naejango.global.common.exception.WebSocketException;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageService messageService;
    private final AuthenticationHandler authenticationHandler;
    private final SubscribeService subscribeService;
    private final WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    /** 특정 채팅 채널을 구독하는 WebSocket Endpoint */
    @SubscribeMapping("/sub/channel/{channelId}")
    public WebSocketMessageReceiveDto subscribeChannel(@DestinationVariable("channelId") Long channelId,
                                                       @Headers SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 읽기 처리를 위한 구독 정보 등록
        String subscriptionId = accessor.getSubscriptionId();
        String sessionId = accessor.getSessionId();
        subscribeService.subscribe(userId, sessionId, subscriptionId, channelId);

        // 유저 개인만 받으면 되기 때문에 Redis 로 Pub/Sub 을 관리하지 않습니다.
        return WebSocketMessageReceiveDto.builder().senderId(userId).channelId(channelId)
                .messageType(MessageType.INFO).content("채널을 구독 합니다.").build();
    }

    /** 특정 채팅 채널을 구독하는 WebSocket Endpoint */
    @SubscribeMapping("/sub/lounge/{channelId}")
    public WebSocketMessageReceiveDto subscribeLoungeChannel(@DestinationVariable("channelId") Long channelId,
                                                             @Headers SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 유저 개인만 받으면 되기 때문에 Redis 로 Pub/Sub 을 관리하지 않습니다.
        return WebSocketMessageReceiveDto.builder().senderId(userId).channelId(channelId)
                .messageType(MessageType.INFO).content("라운지 채널을 구독 합니다.").build();
    }

    /** 채팅 Channel 에 메세지를 보내는 WebSocket Endpoint */
    @MessageMapping("/pub/channel/{channelId}")
    public void sendMessage(@Payload WebSocketMessageReceiveDto messageDto,
                            @DestinationVariable("channelId") Long channelId,
                            @Headers SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.getUserId(accessor.getUser());
        webSocketService.publishMessage(String.valueOf(channelId), messageDto);
        messageService.publishMessage(channelId, userId, MessageType.CHAT, messageDto.getContent());
    }

    /** 웹소켓 통신 중 에러 정보를 수신하는 WebSocket Endpoint */
    @SubscribeMapping("/user/sub/info")
    public WebSocketMessageSendDto subscribeInfoChannel(SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 알림 메세지는 유저 개인만 받으면 되기 때문에 Redis 로 Pub/Sub 을 관리하지 않습니다.
        return WebSocketMessageSendDto.builder().messageType(MessageType.INFO).senderId(userId)
                .content("소켓 통신 정보를 수신합니다.").sentAt(LocalDateTime.now()).build();
    }

    /** 웹소켓 통신 중 발생하는 예외 핸들링 */
    @MessageExceptionHandler
    public void handleException(Throwable e, @Headers SimpMessageHeaderAccessor accessor) {
        System.out.println("Error Catch");
        if (e instanceof WebSocketException) {
            WebSocketException exception = (WebSocketException) e;
            e.printStackTrace();
            messagingTemplate.convertAndSend("/sub/info-user" + accessor.getSessionId(),
                    WebSocketErrorResponse.response(exception.getErrorCode()));
        }
        if (e instanceof CustomException) {
            CustomException exception = (CustomException) e;
            e.printStackTrace();
            messagingTemplate.convertAndSend("/sub/info-user" + accessor.getSessionId(),
                    WebSocketErrorResponse.response(exception.getErrorCode()));
        }
        e.printStackTrace();
        messagingTemplate.convertAndSend("/sub/info-user" + accessor.getSessionId(),
                WebSocketErrorResponse.builder().error(e.getCause().getMessage()).message("통신 중 에러가 발생하였습니다.").build());
    }

}