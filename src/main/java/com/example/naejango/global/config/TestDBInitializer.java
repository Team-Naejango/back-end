package com.example.naejango.global.config;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
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
@Profile("Test")
@RequiredArgsConstructor
public class TestDBInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserProfileRepository userProfileRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChannelRepository channelRepository;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext EntityManager em;

    @Override
    public void run(ApplicationArguments args) {
        chatTestSetup();
    }

    @Transactional
    public void chatTestSetup() {
        transactionTemplate.execute(status -> {
            // 테스트 유저 4명 등록
            User testUser1 = User.builder().role(Role.USER).userKey("test_1").password("").build();
            User testUser2 = User.builder().role(Role.USER).userKey("test_2").password("").build();
            User testUser3 = User.builder().role(Role.USER).userKey("test_3").password("").build();
            User testUser4 = User.builder().role(Role.USER).userKey("test_4").password("").build();

            userRepository.save(testUser1);
            userRepository.save(testUser2);
            userRepository.save(testUser3);
            userRepository.save(testUser4);

            UserProfile userProfile1 = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile2 = UserProfile.builder().nickname("안씨").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile3 = UserProfile.builder().nickname("이씨").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
            UserProfile userProfile4 = UserProfile.builder().nickname("박씨").imgUrl("imgUrl").intro("테스트 유저 4 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();

            userProfileRepository.save(userProfile1);
            userProfileRepository.save(userProfile2);
            userProfileRepository.save(userProfile3);
            userProfileRepository.save(userProfile4);

            testUser1.setUserProfile(userProfile1);
            testUser2.setUserProfile(userProfile2);
            testUser3.setUserProfile(userProfile3);
            testUser3.setUserProfile(userProfile4);

            // 아이템 1개 생성
            Item testItem1 = Item.builder()
                    .name("테스트 아이템1")
                    .itemType(ItemType.INDIVIDUAL_BUY)
                    .viewCount(0)
                    .imgUrl("")
                    .tag("태그1 태그2")
                    .status(true)
                    .description("").build();

            Item testItem2 = Item.builder()
                    .name("테스트 아이템2")
                    .itemType(ItemType.GROUP_BUY)
                    .viewCount(0)
                    .imgUrl("")
                    .tag("태그2 태그3")
                    .status(true)
                    .description("").build();

            itemRepository.save(testItem1);
            itemRepository.save(testItem2);

            // 채팅 채널 생성
            PrivateChannel channel1 = new PrivateChannel();
            GroupChannel channel2 = GroupChannel.builder()
                    .owner(em.getReference(User.class, testUser2.getId()))
                    .item(em.getReference(Item.class, testItem2.getId()))
                    .isClosed(false)
                    .channelLimit(5)
                    .defaultTitle("공동 구매")
                    .build();


            channelRepository.save(channel1);
            channelRepository.save(channel2);

            // 채팅 생성
            // 채팅 채널 1 = chat1, chat2
            // 채팅 채널 2 = chat3, chat4, chat5
            Chat chat1 = Chat.builder().owner(testUser1)
                    .title(testUser2.getUserProfile().getNickname())
                    .channel(channel1).build();

            Chat chat2 = Chat.builder().owner(testUser2)
                    .title(testUser1.getUserProfile().getNickname())
                    .channel(channel1).build();

            Chat chat3 = Chat.builder().owner(testUser2)
                    .title(channel2.getDefaultTitle())
                    .channel(channel2).build();

            Chat chat4 = Chat.builder().owner(testUser3)
                    .title(channel2.getDefaultTitle())
                    .channel(channel2).build();

            Chat chat5 = Chat.builder().owner(testUser4)
                    .title(channel2.getDefaultTitle())
                    .channel(channel2).build();

            chatRepository.save(chat1);
            chatRepository.save(chat2);
            chatRepository.save(chat3);
            chatRepository.save(chat4);
            chatRepository.save(chat5);

            // Message 생성
            Message msg1 = Message.builder().content("메세지1").senderId(testUser2.getId()).build();
            Message msg2 = Message.builder().content("메세지2").senderId(testUser2.getId()).build();
            messageRepository.save(msg1);
            messageRepository.save(msg2);


            // Chat - Message 연결
            ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).isRead(false).chat(chat1).build();
            ChatMessage chatMessage2 = ChatMessage.builder().message(msg1).isRead(true).chat(chat2).build();
            ChatMessage chatMessage3 = ChatMessage.builder().message(msg2).isRead(true).chat(chat3).build();
            ChatMessage chatMessage4 = ChatMessage.builder().message(msg2).isRead(true).chat(chat4).build();
            ChatMessage chatMessage5 = ChatMessage.builder().message(msg2).isRead(false).chat(chat5).build();

            chatMessageRepository.save(chatMessage1);
            chatMessageRepository.save(chatMessage2);
            chatMessageRepository.save(chatMessage3);
            chatMessageRepository.save(chatMessage4);
            chatMessageRepository.save(chatMessage5);

            channel1.updateLastMessage(msg1.getContent());
            channel2.updateLastMessage(msg2.getContent());

            em.flush();
            return null;
        });
    }
}
