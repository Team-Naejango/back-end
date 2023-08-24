package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.ChannelUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelUserRepository extends JpaRepository<ChannelUser, Long> {
}
