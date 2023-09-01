package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT DISTINCT NEW com.example.naejango.domain.chat.dto.ParticipantInfoDto(u.id, up.nickname, up.imgUrl) " +
            "FROM Channel cnl JOIN Chat cht ON cht.channelId = cnl.id JOIN User u ON u.id = cht.ownerId JOIN u.userProfile up")
    List<ParticipantInfoDto> findParticipantsByChannelId(Long channelId);

    Optional<Channel> findChannelByOwnerId(Long ownerId);
}
