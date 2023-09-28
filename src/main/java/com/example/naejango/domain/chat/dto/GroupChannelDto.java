package com.example.naejango.domain.chat.dto;

import com.example.naejango.domain.chat.domain.GroupChannel;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupChannelDto {
    private Long channelId;
    private Long ownerId;
    private Long itemId;
    private String imgUrl;
    private int participantsCount;
    private String defaultTitle;
    private int channelLimit;
    public GroupChannelDto(GroupChannel channel) {
        this.channelId = channel.getId();
        this.ownerId = channel.getOwner().getId();
        this.itemId = channel.getItem().getId();
        this.imgUrl = channel.getItem().getImgUrl();
        this.participantsCount = channel.getParticipantsCount();
        this.defaultTitle = channel.getDefaultTitle();
        this.channelLimit = channel.getChannelLimit();
    }
}
