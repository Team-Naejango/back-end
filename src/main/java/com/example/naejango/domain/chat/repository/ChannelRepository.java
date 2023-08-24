package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Query("SELECT u FROM Channel cnl JOIN cnl.channelUsers cu JOIN cu.user u WHERE cnl.id = :channelId")
    List<User> findUserByChannelId(@Param("channelId") Long channelId);

}
