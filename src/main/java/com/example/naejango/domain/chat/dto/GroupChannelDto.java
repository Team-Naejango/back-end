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
    private Long storageId;
    private int participantsCount;
    private String defaultTitle;
    private int channelLimit; // 방 정원 : 기능 추가 예정

    public GroupChannelDto(GroupChannel channel) {
        this.channelId = channel.getId();
        this.ownerId = channel.getOwnerId();
        this.storageId = channel.getStorageId();
        this.participantsCount = channel.getParticipantsCount();
        this.defaultTitle = channel.getDefaultTitle();
        this.channelLimit = channel.getChannelLimit();
    }
}
