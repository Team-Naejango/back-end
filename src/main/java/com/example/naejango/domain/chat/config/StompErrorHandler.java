package com.example.naejango.domain.chat.config;

import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.example.naejango.global.common.exception.WebSocketException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Configuration
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
        Throwable cause = ex.getCause();
        byte[] payLoad;
        try {
            if (cause instanceof WebSocketException) {
                WebSocketException exception = (WebSocketException) cause;
                payLoad = objectMapper.writeValueAsBytes(WebSocketErrorResponse.response(exception.getErrorCode()));
            } else if (cause instanceof CustomException) {
                CustomException exception = (CustomException) cause;
                payLoad = objectMapper.writeValueAsBytes(WebSocketErrorResponse.response(exception.getErrorCode()));
            } else {
                payLoad = ex.getMessage().getBytes(StandardCharsets.UTF_8);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        accessor.setLeaveMutable(true);
        accessor.setDestination("/sub/info-user" + accessor.getSessionId());
        return MessageBuilder.createMessage(payLoad, accessor.getMessageHeaders());
    }

}
