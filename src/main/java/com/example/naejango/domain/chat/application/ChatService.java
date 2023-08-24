package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.ChannelUser;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChannelUserRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChannelUserRepository channelUserRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;


    @Transactional
    public StartPrivateChatResponseDto createPrivateChat(Long userId1, Long userId2) {
        User user1 = userRepository.findUserWithProfileById(userId1).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User user2 = userRepository.findUserWithProfileById(userId2).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 채팅 채널 개설
        Channel newChannel = new Channel();
        channelRepository.save(newChannel);

        // 유저를 채널에 연결합니다.
        ChannelUser channelUser1 = ChannelUser.builder().channel(newChannel).user(user1).build();
        ChannelUser channelUser2 = ChannelUser.builder().channel(newChannel).user(user2).build();

        channelUserRepository.save(channelUser1);
        channelUserRepository.save(channelUser2);

        // 방 제목을 상대방 닉네임으로 기본 설정합니다.
        Chat user1Chat = Chat.builder().channelId(newChannel.getId()).title(user2.getUserProfile().getNickname()).type(ChatType.PRIVATE).build();
        Chat user2Chat = Chat.builder().channelId(newChannel.getId()).title(user1.getUserProfile().getNickname()).type(ChatType.PRIVATE).build();
        chatRepository.save(user1Chat);
        chatRepository.save(user2Chat);

        return new StartPrivateChatResponseDto(newChannel.getId(), user1Chat.getId());
    }

}
