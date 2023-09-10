package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
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
    ChatMessageRepository chatMessageRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChannelRepository channelRepository;
    @PersistenceContext
    EntityManager em;

    @BeforeEach
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
        testUser3.setUserProfile(userProfile4);

        // 채팅 채널 생성 (lastModifiedTime 을 임의로 주입합니다.)
        PrivateChannel channel1 = PrivateChannel.builder().lastModifiedDate(LocalDateTime.now().minusSeconds(1)).build();
        GroupChannel channel2 = GroupChannel.builder()
                .owner(testUser2)
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
            Page<ChatInfoDto> chat = chatRepository.findChatByOwnerIdOrderByLastChat(owner.getId(), PageRequest.of(0, 1));
            Long channelId = chat.getContent().get(0).getChannelId();

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
            Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(user2.getId(), PageRequest.of(0, 5));

            // then
            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("조회 성공 : 시간순 정렬 확인")
        void test2() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


            // when
            Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(user2.getId(), PageRequest.of(0, 5));

            // then
            assertEquals(2, result.getTotalElements());
            ChatInfoDto chatInfoDto = result.getContent().get(0);
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
            Channel channel = channelRepository.findGroupChannelByOwnerId(user2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

            // when
            Optional<Chat> result = chatRepository.findChatByChannelIdAndOwnerId(channel.getId(), user2.getId());

            // then
            assertTrue(result.isPresent());
            assertEquals(user2.getId(), result.get().getOwner().getId());
        }
    }

}