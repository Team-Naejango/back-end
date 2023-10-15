package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
class MessageRepositoryTest {

    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @PersistenceContext
    EntityManager em;
    User testUser1; User testUser2; User testUser3; User testUser4;
    UserProfile userProfile1; UserProfile userProfile2; UserProfile userProfile3; UserProfile userProfile4;
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

        // 채팅 채널 생성
        channel1 = PrivateChannel.builder().build();
        channel2 = GroupChannel.builder()
                .channelType(ChannelType.GROUP)
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
        Message msg1 = Message.builder().content("첫번째 메세지.").senderId(testUser2.getId()).channel(channel1).build();
        Message msg2 = Message.builder().content("두번째 메세지.").senderId(testUser2.getId()).channel(channel1).build();
        Message msg3 = Message.builder().content("세번째 메세지.").senderId(testUser2.getId()).channel(channel1).build();
        Message msg4 = Message.builder().content("네번째 메세지.").senderId(testUser2.getId()).channel(channel1).build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);
        messageRepository.save(msg3);
        messageRepository.save(msg4);

        // Chat - Message 연결
        ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).isRead(true).chat(chat1).build();
        ChatMessage chatMessage2 = ChatMessage.builder().message(msg2).isRead(true).chat(chat1).build();
        ChatMessage chatMessage3 = ChatMessage.builder().message(msg3).isRead(true).chat(chat1).build();
        ChatMessage chatMessage4 = ChatMessage.builder().message(msg4).isRead(true).chat(chat1).build();
        ChatMessage chatMessage5 = ChatMessage.builder().message(msg1).isRead(true).chat(chat2).build();
        ChatMessage chatMessage6 = ChatMessage.builder().message(msg2).isRead(true).chat(chat2).build();
        ChatMessage chatMessage7 = ChatMessage.builder().message(msg3).isRead(true).chat(chat2).build();
        ChatMessage chatMessage8 = ChatMessage.builder().message(msg4).isRead(true).chat(chat2).build();

        chatMessageRepository.save(chatMessage1);
        chatMessageRepository.save(chatMessage2);
        chatMessageRepository.save(chatMessage3);
        chatMessageRepository.save(chatMessage4);
        chatMessageRepository.save(chatMessage5);
        chatMessageRepository.save(chatMessage6);
        chatMessageRepository.save(chatMessage7);
        chatMessageRepository.save(chatMessage8);

        channel1.updateLastMessage(msg1.getContent());
        channel2.updateLastMessage(msg2.getContent());

        em.flush();
    }

    @Nested
    class findRecentMessages {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User testUser1 = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            List<Chat> chatList = chatRepository.findByOwner(testUser1);
            Chat chat = chatList.stream().filter(c -> c.getTitle().equals("안씨"))
                    .findAny().orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

            // when
            Page<Message> result = messageRepository.findRecentMessages(chat.getId(), Pageable.ofSize(2));

            // then
            assertEquals(2, result.getContent().size());
            assertEquals("네번째 메세지.", result.getContent().get(0).getContent());
        }
    }

    @Nested
    class deleteMessagesByChannelId {
        @Test
        @Transactional
        @DisplayName("삭제 성공")
        void test1() {
            // given
            User testUser2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channel2.getId(), testUser2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

            // when
            List<Chat> chatList = chatRepository.findByChannelId(channel2.getId());
            chatList.forEach(c -> chatMessageRepository.deleteChatMessageByChatId(c.getId()));
            em.flush(); em.clear();
            messageRepository.deleteMessagesByChannelId(channel2.getId());
            em.flush(); em.clear();
            Page<Message> result = messageRepository.findRecentMessages(chat.getId(), Pageable.ofSize(5));

            // then
            assertEquals(0, result.getContent().size());
        }
    }


}