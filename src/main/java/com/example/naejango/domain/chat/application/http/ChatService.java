package com.example.naejango.domain.chat.application.http;

import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.JoinGroupChannelDto;
import com.example.naejango.domain.chat.dto.MessagePublishCommandDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageRepository messageRepository;
    private final WebSocketService webSocketService;
    private final MessageService messageService;
    private final EntityManager em;

    /** 그룹 채널 입장 */
    @Transactional
    public JoinGroupChannelDto joinGroupChannel(Long channelId, Long userId) {
        // Channel 조회
        GroupChannel channel = (GroupChannel) channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // 종료된 채널인지 확인
        if(channel.getIsClosed()) throw new CustomException(ErrorCode.CHANNEL_IS_CLOSED);

        // Chat 조회
        Optional<Chat> groupChatOpt = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId);

        // 이미 참여중인 그룹 채널인지 확인
        if (groupChatOpt.isPresent()) {
            return new JoinGroupChannelDto(false, groupChatOpt.get().getId());
        }

        // 채널 정원 확인
        if (channel.getChannelLimit() <= channel.getParticipantsCount())
            throw new CustomException(ErrorCode.CHANNEL_IS_FULL);

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
        MessagePublishCommandDto commandDto = MessagePublishCommandDto.builder()
                .messageType(MessageType.ENTER)
                .channelId(channelId)
                .senderId(userId)
                .content("채널에 참여하였습니다.").build();

        // 채널에 메세지 발송
        webSocketService.publishMessage(commandDto);

        // 입장 메세지 저장
        messageService.publishMessage(commandDto);

        return new JoinGroupChannelDto(true, newChat.getId());
    }

    /** 내 채팅 리스트 조회 */
    public List<ChatInfoDto> myChatList(Long userId, int page, int size) {
        return chatRepository.findChatByOwnerIdOrderByLastChat(userId, page, size);
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

        // 권한 확인
        if (!userId.equals(chat.getOwner().getId()))
            throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);

        chat.changeTitle(title);
    }

    /** 채널 퇴장 */
    @Transactional
    public void deleteChatByChannelIdAndUserId(Long channelId, Long userId) {
        // Chat 로드
        Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        // Channel 로드
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        deleteChat(userId, chat, channel);
    }

    @Transactional
    public void deleteChatByUser(User user){
        chatRepository.findByOwner(user)
                .forEach(chat -> {
                    Optional<Channel> channelOptional = channelRepository.findByChatId(chat.getId());
                    channelOptional.ifPresent(channel -> deleteChat(user.getId(), chat, channel));
                });
    }

    private void deleteChat(Long userId, Chat chat, Channel channel) {
        // Chat 에 연관된 ChatMessage 를 삭제합니다.
        chatMessageRepository.deleteChatMessageByChat(chat.getId());

        // Chat 을 삭제합니다. (더이상 메세지를 수신하지 못하도록)
        chatRepository.deleteById(chat.getId());


        // 그룹 채팅의 경우
        if (channel.getChannelType() == ChannelType.GROUP) {
            // 그룹 채널로 캐스팅 합니다.
            GroupChannel groupChannel = em.find(GroupChannel.class, channel.getId());

            // 방장 관련 로직이 추가되기 전까지 주석처리
//            if (groupChannel.getOwner().getId().equals(userId) && groupChannel.getIsClosed()) {
//                throw new CustomException(ErrorCode.CHANNEL_IS_OPEN);
//            }

            // Channel 의 참여자 수를 줄입니다.
            groupChannel.decreaseParticipantCount();

            // 만약 채널의 참여자가 0 이 되면 연관된 메세지와 채널을 삭제합니다.
            if (groupChannel.getParticipantsCount() == 0) {
                em.flush();
                messageRepository.deleteMessagesByChannelId(channel.getId());
                channelRepository.deleteById(channel.getId());
                return;
            }

            // 퇴장 메세지 발행
            MessagePublishCommandDto commandDto = MessagePublishCommandDto.builder()
                    .messageType(MessageType.EXIT)
                    .channelId(channel.getId())
                    .senderId(userId)
                    .content("채널에서 퇴장 하였습니다.").build();

            messageService.publishMessage(commandDto);
            webSocketService.publishMessage(commandDto);
        }
    }

}
