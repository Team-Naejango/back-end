package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.user.domain.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Query("SELECT gc FROM GroupChannel gc WHERE gc.item.id = :itemId")
    Optional<GroupChannel> findGroupChannelByItemId(@Param("itemId") Long itemId);

    @Query("SELECT c FROM Channel c JOIN Chat cht ON cht.channel.id = c.id WHERE cht.id = :chatId")
    Optional<Channel> findByChatId(@Param("chatId") Long chatId);


    @EntityGraph(attributePaths = {"item"})
    @Query("SELECT gc FROM GroupChannel gc JOIN gc.item i JOIN i.storage s " +
            "WHERE St_DWithin(:center, s.location, :radius, false) = true AND gc.isClosed = false")
    Page<GroupChannel> findGroupChannelWithItemNearBy(@Param("center") Point center, @Param("radius") int radius, Pageable pageable);


    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up JOIN Chat cht ON cht.owner.id = u.id JOIN cht.channel cnl " +
            "WHERE cnl.id = :channelId")
    List<User> findParticipantsByChannelId(@Param("channelId") Long channelId);

}
