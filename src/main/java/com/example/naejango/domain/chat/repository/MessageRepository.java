package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN m.chatMessages cm JOIN cm.chat c WHERE c.id = :chatId ORDER BY m.createdDate ASC")
    Page<Message> findRecentMessages(@Param("chatId") Long chatId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.channel.id = :channelId")
    void deleteMessagesByChannelId(@Param("channelId")Long channelId);

}
