package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;

    public List<ParticipantInfoDto> findParticipantsInChannel(Long channelId) {
        // 조회 권한 확인
        List<User> participants = channelRepository.findParticipantsByChannelId(channelId);
        return participants.stream().map(participant -> new ParticipantInfoDto(participant.getId(),
                participant.getUserProfile().getNickname(), participant.getUserProfile().getImgUrl())).collect(Collectors.toList());
    }

    public boolean isFull(Long channelId) {
        GroupChannel channel = (GroupChannel) channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        return channel.getChannelLimit() <= channel.getParticipantsCount();
    }
}
