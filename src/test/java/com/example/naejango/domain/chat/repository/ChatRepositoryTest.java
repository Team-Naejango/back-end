package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaAuditing
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

        testUser1.createUserProfile(userProfile1);
        testUser2.createUserProfile(userProfile2);
        testUser3.createUserProfile(userProfile3);
        testUser3.createUserProfile(userProfile4);

        // 채팅 채널 생성
        PrivateChannel channel1 = new PrivateChannel();
        GroupChannel channel2 = GroupChannel.builder()
                .ownerId(testUser2.getId())
                .defaultTitle("공동구매")
                .channelLimit(5)
                .participantsCount(0).build();


        channelRepository.save(channel1);
        channelRepository.save(channel2);

        // 채팅 생성
        // 채팅 채널 1 = chat1, chat2
        // 채팅 채널 2 = chat3, chat4, chat5
        Chat chat1 = Chat.builder().ownerId(testUser1.getId())
                .title(testUser2.getUserProfile().getNickname())
                .channelId(channel1.getId()).chatType(ChatType.PRIVATE).build();

        Chat chat2 = Chat.builder().ownerId(testUser2.getId())
                .title(testUser1.getUserProfile().getNickname())
                .channelId(channel1.getId()).chatType(ChatType.PRIVATE).build();

        Chat chat3 = Chat.builder().ownerId(testUser2.getId())
                .title(channel2.getDefaultTitle())
                .channelId(channel2.getId()).chatType(ChatType.GROUP).build();

        Chat chat4 = Chat.builder().ownerId(testUser3.getId())
                .title(channel2.getDefaultTitle())
                .channelId(channel2.getId()).chatType(ChatType.GROUP).build();

        Chat chat5 = Chat.builder().ownerId(testUser4.getId())
                .title(channel2.getDefaultTitle())
                .channelId(channel2.getId()).chatType(ChatType.GROUP).build();

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

        chat1.updateLastMessage(msg1.getContent());
        chat2.updateLastMessage(msg1.getContent());
        chat3.updateLastMessage(msg2.getContent());
        chat4.updateLastMessage(msg2.getContent());
        chat5.updateLastMessage(msg2.getContent());

        em.flush();
    }

    @Nested
    class findPrivateChannelBetweenUsers {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User owner = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User theOther = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Optional<PrivateChatDto> result = chatRepository.findPrivateChannelBetweenUsers(owner.getId(), theOther.getId());
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
            Optional<PrivateChatDto> result = chatRepository.findPrivateChannelBetweenUsers(user1.getId(), user3.getId());

            // then
            assertTrue(result.isEmpty());
        }


    }

    @Nested
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
    class updateLastMessageByChannelId {
        @Test
        @Transactional
        @DisplayName("업데이트 성공")
        void test1() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Channel channel = channelRepository.findGroupChannelByOwnerId(user2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
            String testMessage = "마지막 대화가 업데이트 되었습니다.";

            // when
            chatRepository.updateLastMessageByChannelId(channel.getId(), testMessage);
            em.flush(); em.clear();

            // then
            List<Chat> result = chatRepository.findByChannelId(channel.getId());
            assertEquals(3, result.size());
            result.forEach(chat -> assertEquals(testMessage, chat.getLastMessage()));
        }
    }

    @Nested
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
            assertEquals(user2.getId(), result.get().getOwnerId());
        }
    }

}