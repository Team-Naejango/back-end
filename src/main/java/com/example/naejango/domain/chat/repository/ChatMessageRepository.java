package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying
    void deleteChatMessageByChatId(Long chatId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chat.id = :chatId")
    void readMessage(@Param("chatId") Long chatId);
}
