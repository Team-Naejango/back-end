package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ChannelRepository")
class ChannelRepositoryTest {
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChannelRepository channelRepository;
    @PersistenceContext
    EntityManager em;

    GeomUtil geomUtil = new GeomUtil();

    @Nested
    @DisplayName("채널 참여자 조회")
    class findParticipantsByChannelId {
        @BeforeEach
        @Transactional
        void setup() {
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
            testUser4.setUserProfile(userProfile4);

            Storage storage = Storage.builder()
                    .name("테스트 창고1")
                    .user(testUser1)
                    .location(geomUtil.createPoint(127.0368, 37.4949))
                    .address("서울시 강남구")
                    .build();

            storageRepository.save(storage);

            Item item1 = Item.builder()
                    .itemType(ItemType.GROUP_BUY)
                    .name("테스트 아이템1")
                    .description("")
                    .status(true)
                    .imgUrl("이미지 링크")
                    .tag("아이")
                    .viewCount(0)
                    .storage(storage)
                    .build();

            itemRepository.save(item1);

            // 채팅 채널 생성
            PrivateChannel channel1 = PrivateChannel.builder().build();
            GroupChannel channel2 = GroupChannel.builder()
                    .channelType(ChannelType.GROUP)
                    .item(item1)
                    .channelLimit(5)
                    .defaultTitle("기본 설정 방제")
                    .owner(testUser2)
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

            // channel 의 lastMessage 변경
            channel1.updateLastMessage(msg1.getContent());
            channel2.updateLastMessage(msg2.getContent());

            em.flush();
        }

        @Test
        @DisplayName("성공")
        void test1(){
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Channel channel = channelRepository.findGroupChannelByOwnerId(user2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

            // when
            List<User> participants = channelRepository.findParticipantsByChannelId(channel.getId());
            System.out.println("participants = " + participants);

            // then
            assertEquals(3, participants.size());
        }
    }

    @Nested
    @DisplayName("근처 채널 조회 ")
    class findGroupChannelWithItemNearBy {
        @BeforeEach
        @Transactional
        void setup() {
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
            testUser4.setUserProfile(userProfile4);

            Storage storage = Storage.builder()
                    .name("테스트 창고1")
                    .user(testUser1)
                    .location(geomUtil.createPoint(127.0368, 37.4949))
                    .address("서울시 강남구")
                    .build();

            storageRepository.save(storage);

            Item item1 = Item.builder()
                    .itemType(ItemType.GROUP_BUY)
                    .name("테스트 아이템1")
                    .description("")
                    .status(true)
                    .imgUrl("이미지 링크")
                    .tag("아이")
                    .viewCount(0)
                    .storage(storage)
                    .build();

            itemRepository.save(item1);

            // 채팅 채널 생성
            PrivateChannel channel1 = PrivateChannel.builder().build();
            GroupChannel channel2 = GroupChannel.builder()
                    .channelType(ChannelType.GROUP)
                    .item(item1)
                    .channelLimit(5)
                    .isClosed(false)
                    .defaultTitle("기본 설정 방제")
                    .owner(testUser2)
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

            // channel 의 lastMessage 변경
            channel1.updateLastMessage(msg1.getContent());
            channel2.updateLastMessage(msg2.getContent());

            em.flush();
        }
        @Test
        @DisplayName("성공")
        void test1(){
            // when
            List<GroupChannel> content = channelRepository.findGroupChannelWithItemNearBy(
                    geomUtil.createPoint(127.0367, 37.4948),
                    1000,
                    PageRequest.of(0, 10)
            ).getContent();

            List<GroupChannelDto> result = content.stream().map(GroupChannelDto::new).collect(Collectors.toList());

            // then
            assertEquals(1, result.size());
            assertEquals("이미지 링크", result.get(0).getImgUrl());
        }
    }

}