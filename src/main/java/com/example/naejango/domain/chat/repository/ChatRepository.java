package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
import com.example.naejango.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT new com.example.naejango.domain.chat.dto.ChatInfoDto(c, SUM(CASE WHEN m.isRead = false THEN 1 ELSE 0 END)) " +
            "FROM Chat c JOIN c.messages m WHERE c.ownerId = :ownerId " +
            "GROUP BY c ORDER BY c.lastModifiedDate DESC")
    Page<ChatInfoDto> findChatByOwnerIdOrderByLastChatTime(@Param("ownerId") Long ownerId, Pageable pageable);

    @Modifying
    @Query("UPDATE Chat c SET c.lastMessage = :msg WHERE c.channelId = :channelId")
    void updateLastMessageByChannelId(@Param("channelId") Long channelId, @Param("msg") String msg);

    @Query("SELECT u FROM Channel cnl JOIN cnl.channelUsers cu JOIN cu.user u WHERE cnl.id = :channelId")
    List<User> findUserByChannelId(@Param("channelId") Long channelId);

    int countByChannelIdAndOwnerId(Long channelId, Long ownerId);
    int countByIdAndOwnerId(Long chatId, Long ownerId);

    List<Chat> findChatByChannelId(Long channelId);

    Optional<Chat> findChatById(Long chatId);

}
