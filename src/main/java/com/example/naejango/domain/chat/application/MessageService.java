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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SubscribeRepository subscribeRepository;
    @PersistenceContext
    private final EntityManager em;

    @Transactional
    public void publishMessage(Long channelId, Long senderId, MessageType messageType, String content) {
        // 메세지를 저장합니다.
        Channel channel = em.getReference(Channel.class, channelId);
        Message sentMessage = Message.builder()
                .messageType(messageType)
                .senderId(senderId)
                .content(content)
                .channel(channel)
                .build();
        messageRepository.save(sentMessage);

        // 메세지를 채팅방에 할당 합니다.
        // 채널에 연결되어 있는 모든 chatId를 찾아 옵니다.
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        chatRepository.findByChannelId(channelId).forEach(chat -> {
            ChatMessage chatMessage = ChatMessage.builder().isRead(false).message(sentMessage).chat(chat).build();
            // 현재 구독중(보고 있는)인 유저의 경우 읽음 처리를 합니다.
            if(subscribers.contains(chat.getOwnerId())) chatMessage.read();
            chatMessageRepository.save(chatMessage);
        });

        // Channel 의 마지막 메세지를 업데이트 합니다.
        channelRepository.setLastMessage(channelId, sentMessage.getId());
    }

    public Page<Message> recentMessages(Long userId, Long chatId, int page, int size) {
        // 보안 로직
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        if(!userId.equals(chat.getOwnerId())) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);
        return messageRepository.findRecentMessages(chatId, PageRequest.of(page, size));
    }

    public void readMessages(Long chatId) {
        chatMessageRepository.readMessage(chatId);
    }
}
