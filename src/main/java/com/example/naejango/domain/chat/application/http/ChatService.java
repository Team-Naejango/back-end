package com.example.naejango.domain.chat.application.http;

import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.JoinGroupChannelDto;
import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final WebSocketService webSocketService;
    private final MessageService messageService;
    private final EntityManager em;


    /** 그룹 채널 입장 */
    @Transactional
    public JoinGroupChannelDto joinGroupChannel(Long channelId, Long userId) {
        // Channel 조회
        GroupChannel channel = (GroupChannel) channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // 채널 정원 확인
        if (channel.getChannelLimit() <= channel.getParticipantsCount())
            throw new CustomException(ErrorCode.CHANNEL_IS_FULL);

        // Chat 조회
        Optional<Chat> groupChatOpt = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId);

        // 이미 참여중인 그룹 채널인지 확인
        if (groupChatOpt.isPresent()) {
            return new JoinGroupChannelDto(false, groupChatOpt.get().getId());
        }

        // Chat 생성
        Chat newChat = Chat.builder()
                .owner(em.getReference(User.class, userId))
                .title(channel.getDefaultTitle())
                .channel(channel)
                .build();
        chatRepository.save(newChat);

        // 채널 참여자 수를 늘립니다.
        channel.increaseParticipantCount();

        // 아래 발행 로직의 정상적 작동을 위해 변경 사항을 DB 에 저장합니다.
        em.flush();
        em.clear();

        // 채널 입장 메세지 생성
        messageService.publishMessage(channelId, userId, MessageType.ENTER, "채널에 참여하였습니다.");

        // 채널에 메세지 발행
        WebSocketMessageDto messageDto = WebSocketMessageDto.builder()
                .messageType(MessageType.ENTER)
                .content("채널에 참여하였습니다.")
                .channelId(channelId)
                .userId(userId).build();
        webSocketService.publishMessage(String.valueOf(channelId), messageDto);

        return new JoinGroupChannelDto(true, newChat.getId());
    }

    /** 내 채팅 리스트 조회 */
    public Page<ChatInfoDto> myChatList(Long userId, int page, int size) {
        return chatRepository.findChatByOwnerIdOrderByLastChat(userId, PageRequest.of(page, size));
    }

    /** 내 Chat ID 조회 */
    public Long myChatId(Long channelId, Long userId) {
        Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        return chat.getId();
    }

    /** 채팅방 제목 수정 */
    @Transactional
    public void changeChatTitle(Long userId, Long chatId, String title) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if (!userId.equals(chat.getOwner().getId()))
            throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chat.changeTitle(title);
    }



}
