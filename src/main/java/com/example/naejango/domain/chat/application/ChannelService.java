package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;

    public Optional<Chat> findChat(Long channelId, Long userId) {
        return chatRepository.findChatByChannelIdAndOwnerId(channelId, userId);
    }

    public List<ParticipantInfoDto> findChatParticipants(Long channelId, Long userId) {
        if(findChat(channelId, userId).isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);
        return channelRepository.findParticipantsByChannelId(channelId);
    }
}
