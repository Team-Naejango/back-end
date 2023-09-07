package com.example.naejango.domain.chat.application;


public interface WebSocketService {
    void publishMessage(String channelId, Object message);
    void subscribeChannel(String channelId, Long userId);
}
