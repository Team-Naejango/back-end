package com.example.naejango.domain.chat.config;

import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.example.naejango.global.common.exception.WebSocketException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
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
        Throwable cause = ex.getCause();
        if (cause instanceof WebSocketException) {
            WebSocketException exception = (WebSocketException) cause;
            try {
                return message(exception.getErrorCode());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if (cause instanceof CustomException) {
            CustomException exception = (CustomException) cause;
            try {
                return message(exception.getErrorCode());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        return message(ex.getMessage());
    }

    private Message<byte[]> message(ErrorCode code) throws JsonProcessingException {
        byte[] payLoad = objectMapper.writeValueAsBytes(WebSocketErrorResponse.response(code));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        return MessageBuilder.createMessage(payLoad, accessor.getMessageHeaders());
    }

    private Message<byte[]> message(String errorMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(errorMessage.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
    }
}
