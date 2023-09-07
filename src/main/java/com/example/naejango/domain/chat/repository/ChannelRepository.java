package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Query("SELECT COUNT (cm) FROM Channel cnl " +
            "JOIN Chat c ON c.channelId = cnl.id JOIN ChatMessage cm ON cm.chat.id = c.id " +
            "WHERE cnl.id = :channelId")
    int countChatMessageByChannelId(@Param("channelId") Long channelId);

    @Query("SELECT gc FROM GroupChannel gc JOIN Storage s ON s.id = gc.storageId " +
            "where St_DWithin(:center, s.location, :radius, false) = true")
    List<GroupChannel> findGroupChannelNearBy(@Param("center") Point center, @Param("radius") int radius);

    @Query("SELECT DISTINCT NEW com.example.naejango.domain.chat.dto.ParticipantInfoDto(u.id, up.nickname, up.imgUrl) " +
            "FROM Channel cnl JOIN Chat cht ON cht.channelId = cnl.id JOIN User u ON u.id = cht.ownerId JOIN u.userProfile up")
    List<ParticipantInfoDto> findParticipantsByChannelId(Long channelId);

    @Modifying
    @Query("UPDATE GroupChannel gc SET gc.participantsCount = gc.participantsCount -1 WHERE gc.id = :channelId")
    void decreaseParticipantsCount(@Param("channelId") Long channelId);

    @Query("SELECT gc FROM GroupChannel gc WHERE gc.ownerId = :ownerId")
    Optional<GroupChannel> findGroupChannelByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT gc FROM GroupChannel gc WHERE gc.id = :channelId AND gc.channelType = com.example.naejango.domain.chat.domain.ChannelType.GROUP")
    Optional<GroupChannel> findGroupChannelById(@Param("channelId") Long channelId);

    @Query("SELECT gc FROM GroupChannel gc WHERE gc.storageId = :storageId")
    Optional<GroupChannel> findGroupChannelByStorageId(Long storageId);

    @Modifying
    @Query("UPDATE Channel c SET c.lastMessageId = :messageId WHERE c.id = :channelId")
    void setLastMessage(@Param("channelId") Long channelId, @Param("messageId") Long messageId);
}
