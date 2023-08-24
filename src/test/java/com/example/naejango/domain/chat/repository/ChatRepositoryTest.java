package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    ChannelUserRepository channelUserRepository;
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
        // 테스트 유저 3명 등록
        User testUser1 = User.builder().role(Role.USER).userKey("test_1").password("").build();
        User testUser2 = User.builder().role(Role.USER).userKey("test_2").password("").build();
        User testUser3 = User.builder().role(Role.USER).userKey("test_3").password("").build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        UserProfile userProfile1 = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19910617").gender(Gender.MALE).phoneNumber("01094862225").build();
        UserProfile userProfile2 = UserProfile.builder().nickname("박씨").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        UserProfile userProfile3 = UserProfile.builder().nickname("이씨").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01023454534").build();

        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);
        userProfileRepository.save(userProfile3);

        testUser1.createUserProfile(userProfile1);
        testUser2.createUserProfile(userProfile2);
        testUser3.createUserProfile(userProfile3);

        em.flush();

        // 채팅 채널 생성
        Channel channel1 = new Channel();
        Channel channel2 = new Channel();

        channelRepository.save(channel1);
        channelRepository.save(channel2);

        // 채널에 유저를 등록합니다.
        ChannelUser channelUser1 = ChannelUser.builder().channel(channel1).user(testUser1).build();
        ChannelUser channelUser2 = ChannelUser.builder().channel(channel1).user(testUser2).build();
        ChannelUser channelUser3 = ChannelUser.builder().channel(channel2).user(testUser2).build();
        ChannelUser channelUser4 = ChannelUser.builder().channel(channel2).user(testUser3).build();

        channelUserRepository.save(channelUser1);
        channelUserRepository.save(channelUser2);
        channelUserRepository.save(channelUser3);
        channelUserRepository.save(channelUser4);

        // 채팅 생성
        // 채팅 채널 1 = chat1, chat2
        // 채팅 채널 2 = chat3, chat4
        Chat chat1 = Chat.builder().ownerId(testUser1.getId())
                .title(testUser2.getUserProfile().getNickname())
                .channelId(channel1.getId()).type(ChatType.PRIVATE).build();

        Chat chat2 = Chat.builder().ownerId(testUser2.getId())
                .title(testUser1.getUserProfile().getNickname())
                .channelId(channel1.getId()).type(ChatType.PRIVATE).build();

        Chat chat3 = Chat.builder().ownerId(testUser2.getId())
                .title(testUser3.getUserProfile().getNickname())
                .channelId(channel2.getId()).type(ChatType.PRIVATE).build();

        Chat chat4 = Chat.builder().ownerId(testUser3.getId())
                .title(testUser2.getUserProfile().getNickname())
                .channelId(channel2.getId()).type(ChatType.PRIVATE).build();

        chatRepository.save(chat1);
        chatRepository.save(chat2);
        chatRepository.save(chat3);
        chatRepository.save(chat4);


        // Message 생성
        Message msg1 = Message.builder().content("메세지1").senderId(testUser2.getId()).build();
        Message msg2 = Message.builder().content("메세지2").senderId(testUser2.getId()).build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);


        // Chat - Message 연결
        ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).chat(chat1).build();
        ChatMessage chatMessage2 = ChatMessage.builder().message(msg1).chat(chat2).build();
        ChatMessage chatMessage3 = ChatMessage.builder().message(msg2).chat(chat3).build();
        ChatMessage chatMessage4 = ChatMessage.builder().message(msg2).chat(chat4).build();

        chatMessageRepository.save(chatMessage1);
        chatMessageRepository.save(chatMessage2);
        chatMessageRepository.save(chatMessage3);
        chatMessageRepository.save(chatMessage4);

        chat1.updateLastMessage(msg1.getContent());
        chat2.updateLastMessage(msg1.getContent());
        chat3.updateLastMessage(msg2.getContent());
        chat4.updateLastMessage(msg2.getContent());
    }

    @Nested
    class findPrivateChatBetweenUsers {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User owner = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User theOther = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Optional<StartPrivateChatResponseDto> result = chatRepository.findPrivateChannelBetweenUsers(owner.getId(), theOther.getId());
            Page<ChatInfoDto> chat = chatRepository.findChatByOwnerIdOrderByLastChatTime(owner.getId(), PageRequest.of(0, 1));
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
            Optional<StartPrivateChatResponseDto> result = chatRepository.findPrivateChannelBetweenUsers(user1.getId(), user3.getId());

            // then
            assertTrue(result.isEmpty());
        }


    }

    @Nested
    class findChatByOwnerId {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // when
            Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChatTime(user2.getId(), PageRequest.of(0, 5));

            // then
            assertEquals(2, result.getTotalElements());
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
            Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChatTime(user2.getId(), PageRequest.of(0, 5));

            // then
            assertEquals(2, result.getTotalElements());
            ChatInfoDto chatInfoDto = result.getContent().get(0);
            assertEquals("이씨", chatInfoDto.getTitle());
            assertEquals("메세지2", chatInfoDto.getLastMessage()); //testUser3("이씨") 랑 나눈 대화("메세지2")가 더 최근
        }
    }




}