package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.dto.ChatInfoDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepositoryCustom {
    List<ChatInfoDto> findChatByOwnerIdOrderByLastChat(Long ownerId, int page, int size);
}
