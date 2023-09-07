package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChannelType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatInfoDto {
    private Long channelId;
    private Long chatId;
    private ChannelType chatType;
    private String title;
    private String lastMessage;
    private long unreadCount;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Chat chat, long unreadCount) {
        this.chatId = chat.getId();
        this.channelId = chat.getChannelId();
        this.chatType = chat.getChatType();
        this.title = chat.getTitle();
        this.lastMessage = chat.getLastMessage();
        this.unreadCount = unreadCount;
        this.lastChatAt = LocalDateTime.now();
    }

}
