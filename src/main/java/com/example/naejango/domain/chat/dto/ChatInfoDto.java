package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
@ToString
public class ChatInfoDto {
    private Long chatId;
    private Long channelId;
    private ChatType type;
    private String title;
    private String lastMessage;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Long chatId, Long channelId, ChatType type, String title, String lastMessage, LocalDateTime lastChatAt) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.type = type;
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastChatAt = lastChatAt;
    }

    public ChatInfoDto(Chat chat) {
        this.chatId = chat.getId();
        this.channelId = chat.getChannelId();
        this.type = chat.getType();
        this.title = chat.getTitle();
        this.lastMessage = chat.getLastMessage();
        this.lastChatAt = LocalDateTime.now();
    }
}
