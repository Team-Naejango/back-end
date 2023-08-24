package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT NEW com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto(c.channelId, c.id) FROM Chat c " +
            "JOIN Channel cnl ON c.channelId = cnl.id " +
            "JOIN cnl.channelUsers cu JOIN cu.user u " +
            "WHERE c.type = com.example.naejango.domain.chat.domain.ChatType.PRIVATE " +
            "AND c.ownerId = :ownerId AND u.id = :theOtherId")
    Optional<StartPrivateChatResponseDto> findPrivateChannelBetweenUsers(@Param("ownerId") Long ownerId, @Param("theOtherId") Long theOtherId);
    @Query("SELECT new com.example.naejango.domain.chat.dto.ChatInfoDto(c.id, c.channelId, c.type, c.title, c.lastMessage, c.lastModifiedDate) " +
            "FROM Chat c WHERE c.ownerId = :ownerId ORDER BY c.lastModifiedDate DESC ")
    Page<ChatInfoDto> findChatByOwnerIdOrderByLastChatTime(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("UPDATE Chat c SET c.lastMessage = :msg WHERE c.channelId = :channelId")
    int updateLastMessageByChannelId(@Param("channelId") Long channelId, @Param("msg") String msg);

    @Query("SELECT c.id FROM Chat c WHERE c.channelId = :channelId")
    List<Long> findChatIdByChannelId(Long channelId);
}
