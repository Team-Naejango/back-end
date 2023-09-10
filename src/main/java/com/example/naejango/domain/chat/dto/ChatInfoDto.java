package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.Chat;
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
    private ChannelType channelType;
    private String title;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Chat chat, Channel channel, long unreadCount) {
        this.channelId = chat.getChannel().getId();
        this.chatId = chat.getId();
        this.channelType = chat.getChannel().getChannelType();
        this.title = chat.getTitle();
        this.unreadCount = unreadCount;
        this.lastMessage = channel.getLastMessage();
        this.lastChatAt = channel.getLastModifiedDate();
    }

}
