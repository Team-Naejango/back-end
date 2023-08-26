package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatInfoDto {
    private Long chatId;
    private Long channelId;
    private ChatType type;
    private String title;
    private String lastMessage;
    private long unreadMessages;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Chat chat, long unreadMessages) {
        this.chatId = chat.getId();
        this.channelId = chat.getChannelId();
        this.type = chat.getType();
        this.title = chat.getTitle();
        this.lastMessage = chat.getLastMessage();
        this.unreadMessages = unreadMessages;
        this.lastChatAt = LocalDateTime.now();
    }

}
