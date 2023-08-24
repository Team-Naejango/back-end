package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatMessage;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
public class MessageService {
    @PersistenceContext
    private EntityManager em;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Message publishMessage(Long channelId, Long senderId, String content) {
        chatRepository.updateLastMessageByChannelId(channelId, content);

        Message sentMessage = Message.builder().senderId(senderId).content(content).build();
        messageRepository.save(sentMessage);

        chatRepository.findChatIdByChannelId(channelId).forEach(chatId -> {
            Chat chat = em.getReference(Chat.class, chatId);
            ChatMessage chatMessage = ChatMessage.builder().message(sentMessage).chat(chat).build();
            chatMessageRepository.save(chatMessage);
            sentMessage.getChatMessage().add(chatMessage);
        });

        return sentMessage;
    }

}
