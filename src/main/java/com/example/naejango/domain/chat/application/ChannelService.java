package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChatRepository chatRepository;

    public boolean isParticipants(Long channelId, Long userId) {
        int i = chatRepository.countByChannelIdAndOwnerId(channelId, userId);
        return i != 0;
    }
}
