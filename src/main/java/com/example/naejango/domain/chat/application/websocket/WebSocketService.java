package com.example.naejango.domain.chat.application.websocket;


import com.example.naejango.domain.chat.dto.MessagePublishCommandDto;

public interface WebSocketService {
    void publishMessage(MessagePublishCommandDto commandDto);
}