package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;

    public List<ParticipantInfoDto> findParticipantsInChannel(Long channelId, Long userId) {
        // 조회 권한 확인
        if(chatRepository.findChatByChannelIdAndOwnerId(channelId, userId).isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);
        return channelRepository.findParticipantsByChannelId(channelId);
    }

    public boolean isFull(Long channelId) {
        GroupChannel channel = (GroupChannel) channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        return channel.getChannelLimit() <= channel.getParticipantsCount();
    }
}
