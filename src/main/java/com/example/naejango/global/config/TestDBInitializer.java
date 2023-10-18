package com.example.naejango.global.config;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
@Profile("WebSocketTestDB")
@Transactional
@RequiredArgsConstructor
public class TestDBInitializer implements ApplicationRunner {
    private final TransactionTemplate transactionTemplate;
    @PersistenceContext EntityManager em;

    @Override
    public void run(ApplicationArguments args) {
        dbSetup();
    }

    @Transactional
    public void dbSetup() {
        transactionTemplate.execute(status -> {
            // 테스트 유저 4명 등록
            User user1 = User.builder().role(Role.USER).userKey("test1").password("").build();
            User user2 = User.builder().role(Role.USER).userKey("test2").password("").build();
            User user3 = User.builder().role(Role.USER).userKey("test3").password("").build();
            User user4 = User.builder().role(Role.USER).userKey("test4").password("").build();
            em.persist(user1);em.persist(user2);em.persist(user3);em.persist(user4);
            UserProfile userProfile1 = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile2 = UserProfile.builder().nickname("안씨").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile3 = UserProfile.builder().nickname("이씨").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile4 = UserProfile.builder().nickname("박씨").imgUrl("imgUrl").intro("테스트 유저 4 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            em.persist(userProfile1);em.persist(userProfile2);em.persist(userProfile3);em.persist(userProfile4);
            user1.setUserProfile(userProfile1);user2.setUserProfile(userProfile2);user3.setUserProfile(userProfile3);user4.setUserProfile(userProfile4);
            Account account1 = Account.builder().user(user1).balance(0).build();
            Account account2 = Account.builder().user(user2).balance(0).build();
            Account account3 = Account.builder().user(user3).balance(0).build();
            Account account4 = Account.builder().user(user4).balance(0).build();
            em.persist(account1);em.persist(account2);em.persist(account3);em.persist(account4);

            // 아이템 1개 생성
            Item item1 = Item.builder().name("테스트 아이템1").itemType(ItemType.INDIVIDUAL_BUY).viewCount(0).imgUrl("").tag("태그1 태그2").status(true).description("").build();
            Item item2 = Item.builder().name("테스트 아이템2").itemType(ItemType.GROUP_BUY).viewCount(0).imgUrl("").tag("태그2 태그3").status(true).description("").build();
            em.persist(item1);em.persist(item2);

            // 채팅 채널 생성
            PrivateChannel channel1 = new PrivateChannel();
            GroupChannel channel2 = GroupChannel.builder().owner(em.getReference(User.class, user2.getId())).item(em.getReference(Item.class, item2.getId())).isClosed(false).channelLimit(5).defaultTitle("공동 구매").build();
            em.persist(channel1);em.persist(channel2);

            // 채팅 생성 / 채팅 채널 1 = chat1, chat2 / 채팅 채널 2 = chat3, chat4, chat5
            Chat chat1 = Chat.builder().owner(user1).title(user2.getUserProfile().getNickname()).channel(channel1).build();
            Chat chat2 = Chat.builder().owner(user2).title(user1.getUserProfile().getNickname()).channel(channel1).build();
            Chat chat3 = Chat.builder().owner(user2).title(channel2.getDefaultTitle()).channel(channel2).build();
            Chat chat4 = Chat.builder().owner(user3).title(channel2.getDefaultTitle()).channel(channel2).build();
            Chat chat5 = Chat.builder().owner(user4).title(channel2.getDefaultTitle()).channel(channel2).build();
            em.persist(chat1);em.persist(chat2);em.persist(chat3);em.persist(chat4);em.persist(chat5);

            // Message 생성
            Message msg1 = Message.builder().content("메세지1").senderId(user2.getId()).build();
            Message msg2 = Message.builder().content("메세지2").senderId(user2.getId()).build();
            em.persist(msg1);em.persist(msg2);

            // Chat - Message 연결
            ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).isRead(false).chat(chat1).build();
            ChatMessage chatMessage2 = ChatMessage.builder().message(msg1).isRead(true).chat(chat2).build();
            ChatMessage chatMessage3 = ChatMessage.builder().message(msg2).isRead(true).chat(chat3).build();
            ChatMessage chatMessage4 = ChatMessage.builder().message(msg2).isRead(true).chat(chat4).build();
            ChatMessage chatMessage5 = ChatMessage.builder().message(msg2).isRead(false).chat(chat5).build();
            em.persist(chatMessage1);em.persist(chatMessage2);em.persist(chatMessage3);em.persist(chatMessage4);em.persist(chatMessage5);
            channel1.updateLastMessage(msg1.getContent());
            channel2.updateLastMessage(msg2.getContent());
            return null;
        });
    }
}
