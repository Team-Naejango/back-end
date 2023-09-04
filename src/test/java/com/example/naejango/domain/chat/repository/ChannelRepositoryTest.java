package com.example.naejango.domain.chat.repository;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {
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

        testUser1.setUserProfile(userProfile1);
        testUser2.setUserProfile(userProfile2);
        testUser3.setUserProfile(userProfile3);
        testUser3.setUserProfile(userProfile4);

        // 채팅 채널 생성
        PrivateChannel channel1 = PrivateChannel.builder().build();
        GroupChannel channel2 = GroupChannel.builder()
                .chatType(ChatType.GROUP)
                .channelLimit(5)
                .defaultTitle("기본 설정 방제")
                .ownerId(testUser2.getId())
                .build();

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
    class findParticipantsByChannelId {
        @Test
        @DisplayName("조회 성공")
        void test1(){
            // given
            User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Channel channel = channelRepository.findGroupChannelByOwnerId(user2.getId()).orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

            // when
            List<ParticipantInfoDto> result = channelRepository.findParticipantsByChannelId(channel.getId());

            // then
            assertEquals(3, result.size());
        }
    }



}