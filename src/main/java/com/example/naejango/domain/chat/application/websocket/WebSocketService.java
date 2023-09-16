package com.example.naejango.domain.chat.application.websocket;


import com.example.naejango.domain.chat.dto.WebSocketMessageCommandDto;

public interface WebSocketService {
    void publishMessage(WebSocketMessageCommandDto commandDto);
}