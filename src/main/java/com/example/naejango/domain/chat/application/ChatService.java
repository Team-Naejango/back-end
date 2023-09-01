package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.CreateGroupChatDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.repository.*;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    @Transactional
    public PrivateChatDto createPrivateChannel(Long userId1, Long userId2) {
        User user1 = userRepository.findUserWithProfileById(userId1).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User user2 = userRepository.findUserWithProfileById(userId2).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 채팅 채널 개설
        Channel newChannel = Channel.builder().type(ChatType.PRIVATE).defaultTitle("일대일 채팅방").build();
        channelRepository.save(newChannel);

        // 채팅방을 생성합니다. 방 제목을 상대방 닉네임으로 기본 설정합니다.
        Chat user1Chat = Chat.builder().channelId(newChannel.getId()).title(user2.getUserProfile().getNickname()).type(ChatType.PRIVATE).build();
        Chat user2Chat = Chat.builder().channelId(newChannel.getId()).title(user1.getUserProfile().getNickname()).type(ChatType.PRIVATE).build();
        chatRepository.save(user1Chat);
        chatRepository.save(user2Chat);

        return new PrivateChatDto(newChannel.getId(), user1Chat.getId());
    }

    @Transactional
    public CreateGroupChatDto createGroupChannel(Long userId, String title, int limit) {
        // 채팅 채널 생성
        Channel newChannel = Channel.builder().defaultTitle(title).channelLimit(limit).type(ChatType.GROUP).ownerId(userId).build();
        channelRepository.save(newChannel);

        // 채팅방을 생성합니다.
        Chat chat = Chat.builder().title(title).channelId(newChannel.getId()).ownerId(userId).build();
        chatRepository.save(chat);

        return new CreateGroupChatDto(newChannel.getId(), chat.getId());
    }

    @Transactional
    public void changeChatTitle(Long userId, Long chatId, String title) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwnerId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chat.changeTitle(title);
    }



    @Transactional
    public void deleteChat(Long userId, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwnerId())) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        chatMessageRepository.deleteChatMessageByChatId(chatId);

        if(chat.getType().equals(ChatType.PRIVATE)){
            int i = channelRepository.countChatMessageByChannelId(chat.getChannelId());
            if(i == 0) {
                chatRepository.delete(chat);
                messageRepository.deleteMessagesByChannelId(chat.getChannelId());
                channelRepository.deleteById(chat.getChannelId());
            }
        }

        if (chat.getType().equals(ChatType.GROUP)) {
            chatRepository.delete(chat);
            int i = channelRepository.countChatMessageByChannelId(chat.getChannelId());
            if(i == 0) {
                messageRepository.deleteMessagesByChannelId(chat.getChannelId());
                channelRepository.deleteById(chat.getChannelId());
            }
        }
    }

    public Page<ChatInfoDto> myChatList(Long userId, int page, int size) {
        return chatRepository.findChatByOwnerIdOrderByLastChat(userId, PageRequest.of(page, size));
    }

    // !! Channel 에 defaultTitle 만들기, type 도 넣기
    @Transactional
    public Long joinGroupChat(Long channelId, Long userId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        Chat newChat = Chat.builder().ownerId(userId).channelId(channelId).title(channel.getDefaultTitle()).lastMessage(null).build();
        chatRepository.save(newChat);
        return newChat.getId();
    }
}
