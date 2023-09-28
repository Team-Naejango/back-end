package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.user.domain.User;
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

    /**
     * 두 유저 사이의 일대일 대화방을 찾습니다.
     * @param ownerId 요청한 유저의 id
     * @param theOtherId 상대 유저의 id
     * @return 채널 id(ChannelId) 와 요청 유저의 채팅방 id(chatId)
     */
    @Query("SELECT NEW com.example.naejango.domain.chat.dto.ChannelAndChatDto(cnl.id, c1.id) " +
            "FROM PrivateChannel cnl JOIN Chat c1 ON c1.channel.id = cnl.id JOIN Chat c2 ON c2.channel.id = cnl.id " +
            "AND c1.owner.id = :ownerId AND c2.owner.id = :theOtherId ")
    Optional<ChannelAndChatDto> findPrivateChannelBetweenUsers(@Param("ownerId") Long ownerId, @Param("theOtherId") Long theOtherId);

    /** 일대일 채널에서 상대방 Chat 을 조회합니다. */
    @Query("SELECT c FROM Chat c WHERE c.channel.id = :channelId AND c.id <> :chatId")
    Optional<Chat> findOtherChatByPrivateChannelId(@Param("channelId") Long channelId, @Param("chatId") Long chatId);

    /**
     * 특정 회원의 id 로 Chat 객체를 마지막 대화시간 순으로 조회합니다.
     * @param ownerId 요청 회원의 id
     * @return chatId, channelId, type, title, lastMessage, unreadMessages, lastChatAt;
     */
    @Query("SELECT NEW com.example.naejango.domain.chat.dto.ChatInfoDto(c, cnl, SUM(CASE WHEN cm.isRead = false THEN 1 ELSE 0 END)) " +
            "FROM Chat c JOIN c.channel cnl LEFT JOIN ChatMessage cm ON cm.chat = c " +
            "WHERE c.owner.id = :ownerId GROUP BY c, cnl ORDER BY cnl.lastModifiedDate DESC ")
    Page<ChatInfoDto> findChatByOwnerIdOrderByLastChat(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * channelId 와 ownerId 로 Chat 을 조회합니다.
     */
    @Query("SELECT c FROM Chat c WHERE c.channel.id = :channelId AND c.owner.id= :ownerId")
    Optional<Chat> findChatByChannelIdAndOwnerId(@Param("channelId") Long channelId, @Param("ownerId") Long ownerId);

    List<Chat> findByChannelId(Long channelId);
    List<Chat> findByOwner(User user);

}
