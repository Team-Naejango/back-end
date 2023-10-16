package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.naejango.domain.chat.domain.QChannel.channel;
import static com.example.naejango.domain.chat.domain.QChat.chat;
import static com.example.naejango.domain.chat.domain.QChatMessage.chatMessage;

public class ChatRepositoryImpl implements ChatRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public ChatRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ChatInfoDto> findChatByOwnerIdOrderByLastChat(Long ownerId, int page, int size) {
        return queryFactory.select(Projections.constructor(ChatInfoDto.class,
                        chat, channel, countUnread()
                        ))
                .from(chat)
                .leftJoin(chat.channel, channel)
                .leftJoin(chatMessage).on(chat.eq(chatMessage.chat))
                .where(chat.owner.id.eq(ownerId))
                .groupBy(chat, channel)
                .orderBy(channel.lastModifiedDate.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    private NumberExpression<Integer> countUnread() {
        return Expressions.numberTemplate(
                Integer.class, "SUM(CASE WHEN {0} = {1} THEN 1 ELSE 0 END)",
                chatMessage.isRead, false);
    }
}
