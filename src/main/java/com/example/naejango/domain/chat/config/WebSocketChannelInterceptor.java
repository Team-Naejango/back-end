package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.common.exception.CustomException;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtValidator jwtValidator;
    private final ChannelService channelService;
    /**
     * 채널 구독 및 메세지 송신 관련 메서드 입니다.
     * 웹소켓 연결된 사용자가 구독 또는 메세지 송신시 요청을 가로채는 Filter 역할을 합니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);
        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            return message;
        }

        if (accessor.getCommand().equals(StompCommand.SUBSCRIBE)) {
            Long subscriberId = getUserIdFromToken(accessor);
            Long channelId = getChannelId(accessor);
            if(channelService.isParticipants(channelId, subscriberId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST);
            accessor.setHeader("userId", String.valueOf(subscriberId));
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (accessor.getCommand().equals(StompCommand.SEND)) {
            Long senderId = getUserIdFromToken(accessor);
            Long channelId = getChannelId(accessor);
            if(channelService.isParticipants(channelId, senderId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST);
            accessor.setHeader("userId", String.valueOf(senderId));
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        return null;
    }

    /**
     * header 에 포함된 accessToken 을 복호화 하여 UserId 를 가지고 옵니다.
     */
    private Long getUserIdFromToken(StompHeaderAccessor headerAccessor) {
        try {
            var validateRequest = jwtValidator.validateTokenInRequest(headerAccessor);
            if (!validateRequest.isValidToken()) throw new CustomException(ErrorCode.UNAUTHORIZED);
            return validateRequest.getUserId();
        } catch (CustomException e) {
            throw new WebSocketException(ErrorCode.TOKEN_DECRYPTION_FAILURE);
        }
    }

    /**
     * 채널 정보 추출 (예외 처리 필요)
     */

    private Long getChannelId(StompHeaderAccessor headerAccessor) {
        String destination = headerAccessor.getDestination();
        String channelId = destination.substring(destination.lastIndexOf('/') + 1);
        return Long.valueOf(channelId);
    }


}