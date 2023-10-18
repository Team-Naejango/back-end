package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.application.websocket.SubscribeService;
import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.dto.SubScribeCommandDto;
import com.example.naejango.domain.chat.dto.MessagePublishCommandDto;
import com.example.naejango.domain.chat.dto.WebSocketMessageSendDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
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

import static com.example.naejango.domain.chat.domain.MessageType.*;

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
    public WebSocketMessageSendDto subscribeChannel(@DestinationVariable("channelId") Long channelId,
                                                       @Headers SimpMessageHeaderAccessor accessor) {
        // 인증 객체에서 userId 를 꺼내옵니다.
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 구독
        SubScribeCommandDto commandDto = new SubScribeCommandDto(userId, accessor, channelId);
        subscribeService.subscribe(commandDto);

        // 유저 개인만 받으면 되기 때문에 Redis 로 Pub/Sub 을 관리하지 않습니다.
        return WebSocketMessageSendDto.builder().sentAt(LocalDateTime.now()).senderId(userId).channelId(channelId)
                .messageType(SUBSCRIBE_CHANNEL).content(SUBSCRIBE_CHANNEL.getDefaultMessage()).build();
    }

    /** 채팅 Channel 에 메세지를 보내는 WebSocket Endpoint */
    @MessageMapping("/pub/channel/{channelId}")
    public void sendMessage(@Payload String content,
                            @DestinationVariable("channelId") Long channelId,
                            @Headers SimpMessageHeaderAccessor accessor) {
        // 유저 로드
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 발송 권한 확인
        if (!subscribeService.isSubscriber(userId, channelId)) {
            throw new WebSocketException(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST);
        }

        MessagePublishCommandDto commandDto = MessagePublishCommandDto.builder()
                .channelId(channelId)
                .senderId(userId)
                .messageType(CHAT)
                .content(content).build();

        // 메세지 발송
        webSocketService.publishMessage(commandDto);

        // 메세지 저장
        messageService.publishMessage(commandDto);
    }

    /** 웹소켓 통신 중 에러 정보를 수신하는 WebSocket Endpoint */
    @SubscribeMapping("/user/sub/info")
    public WebSocketMessageSendDto subscribeInfoChannel(SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.getUserId(accessor.getUser());

        // 알림 메세지는 유저 개인만 받으면 되기 때문에 Redis 로 Pub/Sub 을 관리하지 않습니다.
        return WebSocketMessageSendDto.builder().messageType(SUBSCRIBE_INFO).senderId(userId)
                .content(SUBSCRIBE_INFO.getDefaultMessage()).sentAt(LocalDateTime.now()).build();
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
        } else if (e instanceof CustomException) {
            CustomException exception = (CustomException) e;
            e.printStackTrace();
            messagingTemplate.convertAndSend("/sub/info-user" + accessor.getSessionId(),
                    WebSocketErrorResponse.response(exception.getErrorCode()));
        } else {
            e.printStackTrace();
            if(e.getCause() != null) e = e.getCause();
            messagingTemplate.convertAndSend("/sub/info-user" + accessor.getSessionId(), WebSocketErrorResponse.builder().error(e.getMessage()).message("통신 중 에러가 발생하였습니다.").build());
        }
    }

}