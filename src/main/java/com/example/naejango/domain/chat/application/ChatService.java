package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.CreateGroupChatDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StorageRepository storageRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    @Transactional
    public PrivateChatDto createPrivateChannel(Long requesterId, Long otherUserId) {
        User requester = userRepository.findUserWithProfileById(requesterId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User otherUser = userRepository.findUserWithProfileById(otherUserId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 채팅 채널 개설
        Channel newPrivateChannel = Channel.builder().chatType(ChatType.PRIVATE).build();
        channelRepository.save(newPrivateChannel);

        // 채팅방을 생성합니다. 방 제목을 상대방 닉네임으로 설정합니다.
        Chat requesterChat = Chat.builder()
                .ownerId(requesterId)
                .channelId(newPrivateChannel.getId())
                .chatType(ChatType.PRIVATE)
                .title(otherUser.getUserProfile().getNickname())
                .lastMessage(null)
                .build();

        Chat otherUserChat = Chat.builder()
                .ownerId(otherUserId)
                .channelId(newPrivateChannel.getId())
                .chatType(ChatType.PRIVATE)
                .title(requester.getUserProfile().getNickname())
                .lastMessage(null)
                .build();

        chatRepository.save(requesterChat);
        chatRepository.save(otherUserChat);

        return new PrivateChatDto(newPrivateChannel.getId(), requesterChat.getId());
    }

    @Transactional
    public CreateGroupChatDto createGroupChannel(Long userId, Long storageId, String defaultTitle, int channelLimit) {
        // 보안 체크
        List<Storage> storages = storageRepository.findByUserId(userId);
        if(storages.stream().filter(s -> s.getId().equals(storageId)).findAny().isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);

        // 채팅 채널 개설
        Channel newGroupChannel = GroupChannel.builder()
                .ownerId(userId)
                .storageId(storageId)
                .participantsCount(1)
                .defaultTitle(defaultTitle)
                .channelLimit(channelLimit)
                .build();
        channelRepository.save(newGroupChannel);

        // 채팅방을 생성합니다.
        Chat chat = Chat.builder()
                .ownerId(userId)
                .channelId(newGroupChannel.getId())
                .chatType(ChatType.GROUP)
                .title(defaultTitle)
                .lastMessage(null)
                .build();
        chatRepository.save(chat);

        return new CreateGroupChatDto(newGroupChannel.getId(), chat.getId());
    }

    @Transactional
    public void changeChatTitle(Long userId, Long chatId, String title) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwnerId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chat.changeTitle(title);
    }


    // 테스트 필요
    @Transactional
    public void deleteChat(Long userId, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwnerId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chatMessageRepository.deleteChatMessageByChatId(chatId);

        if(chat.getChatType().equals(ChatType.PRIVATE)){
            int i = channelRepository.countChatMessageByChannelId(chat.getChannelId());
            if(i == 0) {
                chatRepository.delete(chat);
                messageRepository.deleteMessagesByChannelId(chat.getChannelId());
                channelRepository.deleteById(chat.getChannelId());
            }
        }

        if (chat.getChatType().equals(ChatType.GROUP)) {
            chatRepository.delete(chat);
            channelRepository.decreaseParticipantsCount(chat.getChannelId());
            int i = channelRepository.countChatMessageByChannelId(chat.getChannelId());
            if(i == 0) {
                messageRepository.deleteMessagesByChannelId(chat.getChannelId());
                channelRepository.deleteById(chat.getChannelId());
            }
        }
    }

    @Transactional
    public Long joinGroupChat(Long channelId, Long userId) {
        GroupChannel channel = channelRepository.findGroupChannelById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        Chat newChat = Chat.builder()
                .ownerId(userId)
                .channelId(channelId)
                .chatType(ChatType.GROUP)
                .title(channel.getDefaultTitle())
                .lastMessage(null)
                .build();
        chatRepository.save(newChat);
        channel.join();
        return newChat.getId();
    }
}
