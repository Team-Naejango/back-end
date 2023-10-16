package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("Test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryTest {

    @Autowired
    ChatRepository chatRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
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

    User testUser1; User testUser2; User testUser3; User testUser4;
    UserProfile userProfile1; UserProfile userProfile2; UserProfile userProfile3; UserProfile userProfile4;
    Item item;
    PrivateChannel channel1; GroupChannel channel2;

    @BeforeEach
    void setup() {
        // 테스트 유저 4명 등록
        testUser1 = User.builder().role(Role.USER).userKey("test_1").password("").build();
        testUser2 = User.builder().role(Role.USER).userKey("test_2").password("").build();
        testUser3 = User.builder().role(Role.USER).userKey("test_3").password("").build();
        testUser4 = User.builder().role(Role.USER).userKey("test_4").password("").build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);

        userProfile1 = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        userProfile2 = UserProfile.builder().nickname("안씨").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        userProfile3 = UserProfile.builder().nickname("이씨").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        userProfile4 = UserProfile.builder().nickname("박씨").imgUrl("imgUrl").intro("테스트 유저 4 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();

        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);
        userProfileRepository.save(userProfile3);
        userProfileRepository.save(userProfile4);

        testUser1.setUserProfile(userProfile1);
        testUser2.setUserProfile(userProfile2);
        testUser3.setUserProfile(userProfile3);
        testUser3.setUserProfile(userProfile4);

        // 공동구매 아이템 생성
        item = Item.builder()
                .name("테스트").description("테스트 창고")
                .imgUrl("url").tag("")
                .viewCount(0).status(true)
                .itemType(ItemType.GROUP_BUY).build();

        itemRepository.save(item);

        // 채팅 채널 생성 (lastModifiedTime 을 임의로 주입합니다.)
        channel1 = PrivateChannel.builder()
                .channelType(ChannelType.PRIVATE)
                .lastModifiedDate(LocalDateTime.now().minusSeconds(1))
                .build();

        channel2 = GroupChannel.builder()
                .channelType(ChannelType.GROUP)
                .owner(testUser2)
                .item(item)
                .defaultTitle("공동구매")
                .channelLimit(5)
                .participantsCount(0)
                .lastModifiedDate(LocalDateTime.now()).build();

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
        em.flush();

        channel2.updateLastMessage(msg2.getContent());
        em.flush();
    }

    @Nested
    @DisplayName("유저간 일대일 채널 조회")
    class findPrivateChannelBetweenUsers {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User owner = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User theOther = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Optional<ChannelAndChatDto> result = chatRepository.findPrivateChannelBetweenUsers(owner.getId(), theOther.getId());
            List<ChatInfoDto> chat = chatRepository.findChatByOwnerIdOrderByLastChat(owner.getId(), 0, 1);
            Long channelId = chat.get(0).getChannelId();

            // then
            assertTrue(result.isPresent());
            assertSame(result.get().getChannelId(), channelId);
        }

        @Test
        @DisplayName("조회 실패")
        void test2() {
            // given
            User user1 = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User user3 = userRepository.findByUserKey("test_3").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Optional<ChannelAndChatDto> result = chatRepository.findPrivateChannelBetweenUsers(user1.getId(), user3.getId());

            // then
            assertTrue(result.isEmpty());
        }

    }

    @Nested
    @DisplayName("진행 중인 채팅 조회 - 시간 순")
    class findChatByOwnerIdOrderByLastChat {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            List<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(user2.getId(), 0, 5);

            // then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("조회 성공 : 시간순 정렬 확인")
        void test2() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


            // when
            List<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(user2.getId(), 0, 5);

            // then
            assertEquals(2, result.size());
            ChatInfoDto chatInfoDto = result.get(0);
            assertEquals("메세지2", chatInfoDto.getLastMessage());
        }
    }

    @Nested
    @DisplayName("채널 ID, 유저 ID 로 채팅 조회")
    class findChatIdByChannelIdAndOwnerId {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Optional<Chat> result = chatRepository.findChatByChannelIdAndOwnerId(channel2.getId(), user2.getId());

            // then
            assertTrue(result.isPresent());
            assertEquals(user2.getId(), result.get().getOwner().getId());
        }
    }

}