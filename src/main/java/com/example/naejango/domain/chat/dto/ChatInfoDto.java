package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
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
    private int participantsCount;
    private int channelLimit;
    private String lastMessage;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Chat chat, Channel channel, long unreadCount) {
        this.channelId = chat.getChannel().getId();
        this.chatId = chat.getId();
        this.channelType = chat.getChannel().getChannelType();
        this.title = chat.getTitle();
        this.unreadCount = unreadCount;
        this.participantsCount = channel.getChannelType().equals(ChannelType.GROUP)? ((GroupChannel)channel).getParticipantsCount() : 2;
        this.channelLimit = channel.getChannelType().equals(ChannelType.GROUP)? ((GroupChannel)channel).getChannelLimit() : 2;
        this.lastMessage = channel.getLastMessage();
        this.lastChatAt = channel.getLastModifiedDate();
    }

}
