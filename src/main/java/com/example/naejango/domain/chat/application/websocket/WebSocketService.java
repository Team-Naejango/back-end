package com.example.naejango.domain.chat.application.websocket;


public interface WebSocketService {
    void publishMessage(String channelId, Object message);
}
