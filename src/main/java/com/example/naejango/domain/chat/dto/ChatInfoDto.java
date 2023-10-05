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
    private ChannelType channelType;
    private int participantsCount;
    private int channelLimit;
    private Long itemId;
    private Long chatId;
    private String title;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastChatAt;

    public ChatInfoDto(Chat chat, Channel channel, long unreadCount) {
        this.channelId = chat.getChannel().getId();
        this.channelType = chat.getChannel().getChannelType();
        this.participantsCount = channel.getChannelType().equals(ChannelType.GROUP)? ((GroupChannel)channel).getParticipantsCount() : 0;
        this.channelLimit = channel.getChannelType().equals(ChannelType.GROUP)? ((GroupChannel)channel).getChannelLimit() : 0;
        this.itemId = channel.getChannelType().equals(ChannelType.GROUP)? ((GroupChannel)channel).getItem().getId() : 0L;
        this.chatId = chat.getId();
        this.title = chat.getTitle();
        this.unreadCount = unreadCount;
        this.lastMessage = channel.getLastMessage();
        this.lastChatAt = channel.getLastModifiedDate();
    }

}
