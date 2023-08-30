package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.ChatMessage;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void publishMessage(Long channelId, Long senderId, String content, Set<Long> subscriberIds) {
        // 메세지를 저장합니다.
        Message sentMessage = Message.builder().senderId(senderId).content(content).build();
        messageRepository.save(sentMessage);

        // 메세지를 채팅방에 할당 합니다.
        // 채널에 연결되어 있는 모든 chatId를 찾아 옵니다.
        chatRepository.findChatByChannelId(channelId).forEach(chat -> {
            ChatMessage chatMessage = ChatMessage.builder().isRead(false).message(sentMessage).chat(chat).build();
            // 현재 구독중(보고 있는)인 유저의 경우 읽음 처리를 합니다.
            if(subscriberIds.contains(chat.getOwnerId())) chatMessage.read();
            chatMessageRepository.save(chatMessage);
        });

        // 각각의 Chat 에 마지막 메세지를 업데이트 합니다.
        chatRepository.updateLastMessageByChannelId(channelId, content);
    }

}
