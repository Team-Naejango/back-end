package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    ChannelUserRepository channelUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
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

        UserProfile userProfile1 = UserProfile.builder().nickname("김기홍").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").birth("19910617").gender(Gender.MALE).phoneNumber("01094862225").build();
        UserProfile userProfile2 = UserProfile.builder().nickname("이태용").imgUrl("imgUrl").intro("테스트 유저 2 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        UserProfile userProfile3 = UserProfile.builder().nickname("안세준").imgUrl("imgUrl").intro("테스트 유저 3 입니다.").birth("19900000").gender(Gender.MALE).phoneNumber("01023454534").build();

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
        Message msg1 = Message.builder().content("안녕하세요").senderId(testUser1.getId()).build();
        Message msg2 = Message.builder().content("반갑습니다").senderId(testUser2.getId()).build();
        Message msg3 = Message.builder().content("이태용입니다").senderId(testUser2.getId()).build();
        Message msg4 = Message.builder().content("안세준입니다").senderId(testUser3.getId()).build();

        messageRepository.save(msg1);
        messageRepository.save(msg2);
        messageRepository.save(msg3);
        messageRepository.save(msg4);

        // Chat - Message 연결
        ChatMessage chatMessage1 = ChatMessage.builder().message(msg1).chat(chat1).build();
        ChatMessage chatMessage2 = ChatMessage.builder().message(msg1).chat(chat2).build();
        ChatMessage chatMessage3 = ChatMessage.builder().message(msg2).chat(chat1).build();
        ChatMessage chatMessage4 = ChatMessage.builder().message(msg2).chat(chat2).build();
        ChatMessage chatMessage5 = ChatMessage.builder().message(msg3).chat(chat1).build();
        ChatMessage chatMessage6 = ChatMessage.builder().message(msg3).chat(chat2).build();
        ChatMessage chatMessage7 = ChatMessage.builder().message(msg4).chat(chat3).build();
        ChatMessage chatMessage8 = ChatMessage.builder().message(msg4).chat(chat4).build();

        chatMessageRepository.save(chatMessage1);
        chatMessageRepository.save(chatMessage2);
        chatMessageRepository.save(chatMessage3);
        chatMessageRepository.save(chatMessage4);
        chatMessageRepository.save(chatMessage5);
        chatMessageRepository.save(chatMessage6);
        chatMessageRepository.save(chatMessage7);
        chatMessageRepository.save(chatMessage8);

        chat1.updateLastMessage(msg3.getContent());
        chat2.updateLastMessage(msg3.getContent());
        chat3.updateLastMessage(msg4.getContent());
        chat4.updateLastMessage(msg4.getContent());
    }

    @Nested
    class findRecentMessages {
        @Test
        @DisplayName("조회 성공")
        void test1() {
            // given
            User testUser2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            List<ChatInfoDto> chatInfos = chatRepository.findChatByOwnerIdOrderByLastChat(testUser2.getId(), PageRequest.of(0, 3)).getContent();
            Long chatId = chatInfos.get(1).getChatId(); //  chat2 의 id 가 나올 것

            // when
            Page<Message> recentMessages = messageRepository.findRecentMessages(chatId, PageRequest.of(0, 3));
            List<Message> content = recentMessages.getContent();

            // then
            content.forEach(msg -> System.out.println("msg = " + msg));
            assertEquals(3, content.size());
            assertEquals("이태용입니다", content.get(0).getContent());
        }
    }


}