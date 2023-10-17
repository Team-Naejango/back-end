package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, ChatRepositoryCustom {

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
     * channelId 와 ownerId 로 Chat 을 조회합니다.
     */
    @Query("SELECT c FROM Chat c WHERE c.channel.id = :channelId AND c.owner.id= :ownerId")
    Optional<Chat> findChatByChannelIdAndOwnerId(@Param("channelId") Long channelId, @Param("ownerId") Long ownerId);

    List<Chat> findByChannelId(Long channelId);
    List<Chat> findByOwner(User user);

}
