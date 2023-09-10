package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.repository.*;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SubscribeRepository subscribeRepository;

    @Transactional
    public void publishMessage(Long channelId, Long senderId, MessageType messageType, String content) {
        // 메세지를 저장합니다.
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        Message sentMessage = Message.builder()
                .messageType(messageType)
                .senderId(senderId)
                .content(content)
                .channel(channel)
                .build();
        messageRepository.save(sentMessage);

        // 메세지를 채팅방에 할당 합니다.
        // 현재 메시지를 구독  중인(보고 있는) 구독자를 찾아옵니다.
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        // 채널에 연결되어 있는 모든 chatId를 찾아 옵니다.
        chatRepository.findByChannelId(channelId).forEach(chat -> {
            ChatMessage chatMessage = ChatMessage.builder().isRead(false).message(sentMessage).chat(chat).build();
            // 현재 구독중인(보고 있는) 유저의 경우 읽음 처리를 합니다.
            if(subscribers.contains(chat.getOwner().getId())) chatMessage.read();
            chatMessageRepository.save(chatMessage);
        });

        // Channel 의 마지막 메세지를 업데이트 합니다.
        channel.updateLastMessage(content);
    }

    public Page<Message> recentMessages(Long userId, Long chatId, int page, int size) {
        // 보안 로직
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwner().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);
        return messageRepository.findRecentMessages(chatId, PageRequest.of(page, size));
    }

    public void readMessages(Long chatId) {
        chatMessageRepository.readMessage(chatId);
    }
}
