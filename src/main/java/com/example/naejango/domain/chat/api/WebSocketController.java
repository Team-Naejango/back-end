package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.MessageService;
import com.example.naejango.domain.chat.application.WebSocketService;
import com.example.naejango.domain.chat.dto.request.SendMessageRequestDto;
import com.example.naejango.domain.chat.dto.response.SendMessageResponseDto;
import com.example.naejango.domain.chat.dto.response.SubscribeResponseDto;
import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.example.naejango.global.common.exception.WebSocketException;
import com.example.naejango.global.common.util.AuthenticationHandler;
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

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageService messageService;
    private final AuthenticationHandler authenticationHandler;
    private final SubscribeRepository subscribeRepository;
    private final WebSocketService webSocketService;

    /** 웹소켓 통신 중 정보를 수신하는 채널을 구독하는 Endpoint */
    @SubscribeMapping("/user/queue/info")
    public SubscribeResponseDto subscribeInfoChannel(SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
        return SubscribeResponseDto.builder().userId(userId).message("소켓 통신 정보를 수신합니다.").build();
    }

    /** 채팅 Channel 을 구독하는 Endpoint */
    @SubscribeMapping("/topic/channel/{channelId}")
    public SubscribeResponseDto subscribeChatChannel(@DestinationVariable("channelId") Long channelId, SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
        // 구독 정보 등록
        String subscriptionId = accessor.getSubscriptionId();
        webSocketService.subscribe(userId, subscriptionId, channelId);
        return SubscribeResponseDto.builder().userId(userId).message("채팅 채널 구독을 시작합니다.").subscribingChannelId(channelId).build();
    }

    /** 채팅 Channel 에 메세지를 보내는 Endpoint */
    @MessageMapping("/chat/channel/{channelId}")
    @SendTo("/topic/channel/{channelId}")
    public SendMessageResponseDto sendMessage(@RequestBody SendMessageRequestDto requestDto,
                                              @DestinationVariable("channelId") Long channelId,
                                              SimpMessageHeaderAccessor accessor) {
        Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
        messageService.publishMessage(channelId, userId, requestDto.getContent(), subscribeRepository.findSubscribersByChannelId(channelId));
        return new SendMessageResponseDto(userId, channelId, requestDto.getContent());
    }

    /** 웹소켓 통신 중 발생하는 예외를 핸들링합니다. */
    @MessageExceptionHandler
    @SendToUser("/queue/info")
    public WebSocketErrorResponse handleException(Throwable e) {
        if (e instanceof WebSocketException) {
            WebSocketException exception = (WebSocketException) e;
            e.printStackTrace();
            return WebSocketErrorResponse.response(exception.getErrorCode());
        }
        e.printStackTrace();
        return WebSocketErrorResponse.builder().error(e.getMessage()).message("통신 중 에러가 발생하였습니다.").build();
    }

}