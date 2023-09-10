package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
    private final ChatMessageRepository chatMessageRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final MessageService messageService;
    private final EntityManager em;



    @Transactional
    public ChannelAndChatDto createPrivateChannel(Long requesterId, Long otherUserId) {
        User requester = userRepository.findUserWithProfileById(requesterId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User otherUser = userRepository.findUserWithProfileById(otherUserId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 채팅 채널 개설
        PrivateChannel newPrivateChannel = PrivateChannel.builder().channelType(ChannelType.PRIVATE).build();
        channelRepository.save(newPrivateChannel);

        // 채팅방을 생성합니다. 방 제목을 상대방 닉네임으로 설정합니다.
        Chat requesterChat = Chat.builder()
                .owner(requester)
                .channel(newPrivateChannel)
                .title(otherUser.getUserProfile().getNickname())
                .build();

        Chat otherUserChat = Chat.builder()
                .owner(otherUser)
                .channel(newPrivateChannel)
                .title(requester.getUserProfile().getNickname())
                .build();

        chatRepository.save(requesterChat);
        chatRepository.save(otherUserChat);

        // 채널 시작 메세지를 생성합니다.
        messageService.publishMessage(newPrivateChannel.getId(), null, MessageType.INFO, "채팅이 시작 되었습니다.");

        return new ChannelAndChatDto(newPrivateChannel.getId(), requesterChat.getId());
    }

    @Transactional
    public ChannelAndChatDto createGroupChannel(Long userId, Long itemId, String defaultTitle, int channelLimit) {
        // 그룹 채널 개설
        Channel newGroupChannel = GroupChannel.builder()
                .owner(em.getReference(User.class, userId))
                .item(em.getReference(Item.class, itemId))
                .channelType(ChannelType.GROUP)
                .participantsCount(1)
                .defaultTitle(defaultTitle)
                .channelLimit(channelLimit)
                .build();
        channelRepository.save(newGroupChannel);

        // 채팅방을 생성합니다.
        Chat chat = Chat.builder()
                .owner(em.getReference(User.class, userRepository))
                .channel(newGroupChannel)
                .title(defaultTitle)
                .build();
        chatRepository.save(chat);

        // 채널 시작 메세지를 생성합니다.
        messageService.publishMessage(newGroupChannel.getId(), null, MessageType.INFO, "채팅이 시작되었습니다.");
        return new ChannelAndChatDto(newGroupChannel.getId(), chat.getId());
    }

    @Transactional
    public void changeChatTitle(Long userId, Long chatId, String title) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwner().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chat.changeTitle(title);
    }


    // 테스트 필요
    @Transactional
    public void deleteChat(Long chatId) {
        // Chat, Channel 로드
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        Channel channel = channelRepository.findByChatId(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // Chat 에 연관된 ChatMessage 를 삭제합니다.
        chatMessageRepository.deleteChatMessageByChatId(chatId);

        // 일대일 채널의 경우
        if(channel.getChannelType().equals(ChannelType.PRIVATE)){
            // 상대방이 Chat 이 없거나 ChatMessage 가 없는지 확인
            Optional<Chat> otherChat = chatRepository.findOtherChatByPrivateChannelId(channel.getId(), chat.getId());
            if (otherChat.isEmpty() || !chatMessageRepository.existsByChatId(otherChat.get().getId())) {
                // Chat 삭제
                chatRepository.delete(chat);
                // Channel 의 모든 message 삭제
                messageRepository.deleteMessagesByChannelId(channel.getId());
                // Channel 삭제
                channelRepository.deleteById(channel.getId());
            }
        }

        // 그룹 채팅의 경우
        if (channel.getChannelType().equals(ChannelType.GROUP)) {
            // 그룹 채널로 캐스팅 합니다.
            GroupChannel groupChannel = (GroupChannel) channel;
            // Chat 을 삭제합니다. (더이상 메세지를 수신하지 못하도록)
            chatRepository.delete(chat);
            // Channel 의 참여자 수를 줄입니다.
            groupChannel.decreaseParticipantCount();
            // 만약 채널의 참여자가 0 이 되면 연관된 메세지와 채널을 삭제합니다.
            if(groupChannel.getParticipantsCount() == 0) {
                messageRepository.deleteMessagesByChannelId(channel.getId());
                channelRepository.deleteById(channel.getId());
            }
        }
    }

    /**
     * 그룹 채널에 입장합니다.
     * Controller 계층에서 정원이 초과되지 않을 때 호출됩니다.
     */
    @Transactional
    public Long joinGroupChat(Long channelId, Long userId) {
        // 채널을 조회합니다.
        GroupChannel channel = channelRepository.findGroupChannelById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        // Chat 을 생성합니다.
        Chat newChat = Chat.builder()
                .owner(em.getReference(User.class, userId))
                .title(channel.getDefaultTitle())
                .channel(channel)
                .build();
        chatRepository.save(newChat);

        // 채널 참여자 수를 늘립니다.
        channel.increaseParticipantCount();

        // 아래 발행 로직의 정상적 작동을 위해 변경 사항을 DB 에 저장합니다.
        em.flush(); em.clear();

        // 채널 입장 메세지를 생성합니다.
        messageService.publishMessage(channelId, userId, MessageType.ENTER, "채널에 참여하였습니다.");

        // 채널에 메세지를 발행합니다.
        WebSocketMessageDto messageDto = WebSocketMessageDto.builder()
                .messageType(MessageType.ENTER)
                .content("채널에 참여하였습니다.")
                .channelId(channelId)
                .userId(userId).build();
        webSocketService.publishMessage(String.valueOf(channelId), messageDto);

        return newChat.getId();
    }
}
