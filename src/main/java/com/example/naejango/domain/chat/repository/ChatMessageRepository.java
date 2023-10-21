package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.chat.id = :chatId")
    void deleteChatMessageByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chat.id = :chatId")
    void readMessage(@Param("chatId") Long chatId);

    // 테스트 용
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat.id = :chatId")
    List<ChatMessage> findByChatId(@Param("chatId")Long chatId);

}
