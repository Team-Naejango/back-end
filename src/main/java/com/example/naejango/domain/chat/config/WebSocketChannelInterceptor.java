package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.application.websocket.SubscribeService;
import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

@SuppressWarnings("NullableProblems")
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    private final SubscribeService subscribeService;
    private final JwtAuthenticator jwtAuthenticator;
    private final SubscribeRepository subscribeRepository;

    /**
     * 웹소켓 EndPoint 로 전송되는 메세지를 Intercept 하여
     * 사용자 보안 인증(Token 검사), 구독 취소 등의 기능을 수행합니다.
     * @param message intercept 한 메세지 정보를 담고 있습니다.
     * @param channel 또다른 message 를 보낼 수 있는 창구 입니다.
     * @return 메서드 로직을 거친 message 를 반환합니다. null 인 경우 메세지는 전송되지 않습니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        /* 이미 인증된 회원인 경우 Authentication 객체를 넣어줍니다. */
        authenticate(accessor);

        /* 웹소켓 연결 요청 */
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 이미 세션에 유저가 할당되어 있는 회원은 예외 반환
            subscribeRepository.findUserIdBySessionId(accessor.getSessionId())
                    .ifPresent(userId -> { throw new WebSocketException(ErrorCode.SESSION_ALREADY_EXIST); });

            // 인증 처리
            Authentication authentication = jwtAuthenticator.authenticateWebSocketRequest(accessor);

            // 인증 객체를 sessionId 에 저장합니다.
            subscribeRepository.saveUserIdBySessionId((Long) authentication.getPrincipal(), accessor.getSessionId());
        }

        /* 구독 취소 요청 */
        else if(StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())){
            // 구독 취소
            String subscriptionId = accessor.getSubscriptionId();
            String sessionId = accessor.getSessionId();
            subscribeService.unsubscribe(sessionId, subscriptionId);
        }

        /* 웹소켓 연결 종료 요청 */
        else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 모든 구독 정보 삭제
            subscribeService.disconnect(accessor.getSessionId());
        }

        return generateMessage(message, accessor);
    }

    /* sessionId 로 User 를 로드합니다. */
    private void authenticate(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        subscribeRepository.findUserIdBySessionId(sessionId).ifPresent
                (userId -> accessor.setUser(new UsernamePasswordAuthenticationToken(userId,
                        null, Collections.singletonList(() -> "Role_CHATUSER"))));
    }

    /* message payload 와 accessor 의 header 정보를 조합하여 message 를 생성합니다. */
    private Message<?> generateMessage(Message<?> message, StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }
}