package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
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

    /**
     * 두 유저 사이의 일대일 대화방을 찾습니다.
     * @param ownerId 요청한 유저의 id
     * @param theOtherId 상대 유저의 id
     * @return 채널 id(ChannelId) 와 요청 유저의 채팅방 id(chatId)
     */
    @Query("SELECT NEW com.example.naejango.domain.chat.dto.PrivateChatDto(cnl.id, c1.id) " +
            "FROM Channel cnl JOIN Chat c1 ON c1.channelId = cnl.id JOIN Chat c2 ON c2.channelId = cnl.id " +
            "WHERE c2.type = com.example.naejango.domain.chat.domain.ChatType.PRIVATE " +
            "AND c1.ownerId = :ownerId AND c2.ownerId = :theOtherId ")
    Optional<PrivateChatDto> findPrivateChannelBetweenUsers(@Param("ownerId") Long ownerId, @Param("theOtherId") Long theOtherId);

    /**
     * 특정 회원의 id 로 Chat 객체를 마지막 대화시간 순으로 조회합니다.
     * @param ownerId 요청 회원의 id
     * @return chatId, channelId, type, title, lastMessage, unreadMessages, lastChatAt;
     */
    @Query("SELECT new com.example.naejango.domain.chat.dto.ChatInfoDto(c, SUM(CASE WHEN cm.isRead = false THEN 1 ELSE 0 END)) "+
            "FROM Chat c JOIN c.chatMessages cm WHERE c.ownerId = :ownerId " +
            "GROUP BY c ORDER BY c.lastModifiedDate DESC ")
    Page<ChatInfoDto> findChatByOwnerIdOrderByLastChat(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * Channel 과 연관된 모든 Chat 의 마지막 대화 내용을 업데이트 합니다.
     * @param channelId 채널 id
     * @param msg 메세지 내용
     */
    @Modifying
    @Query("UPDATE Chat c SET c.lastMessage = :msg WHERE c.channelId = :channelId")
    void updateLastMessageByChannelId(@Param("channelId") Long channelId, @Param("msg") String msg);

    /**
     * channelId 와 ownerId 로 Chat 을 조회합니다.
     */
    @Query("SELECT c FROM Chat c WHERE c.channelId = :channelId AND c.ownerId = :ownerId")
    Optional<Chat> findChatByChannelIdAndOwnerId(@Param("channelId") Long channelId, @Param("ownerId") Long ownerId);

    @Query("SELECT c FROM Chat c WHERE c.channelId = :channelId")
    List<Chat> findChatByChannelId(@Param("channelId") Long channelId);

    @Query("SELECT c FROM Chat c WHERE c.channelId = :channelId AND " +
            "c.ownerId = :userId AND c.type = com.example.naejango.domain.chat.domain.ChatType.GROUP")
    Optional<Long> findGroupChat(@Param("channelId")Long channelId, @Param("userId") Long userId);
}
