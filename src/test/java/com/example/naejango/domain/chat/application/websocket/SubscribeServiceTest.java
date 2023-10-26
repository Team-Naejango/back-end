package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.SubScribeCommandDto;
import com.example.naejango.domain.chat.repository.*;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubscribeServiceTest {
    @Autowired SubscribeService subscribeService;
    @Autowired UserRepository userRepository;
    @Autowired ChatRepository chatRepository;
    @Autowired ChannelRepository channelRepository;
    @Autowired SubscribeRepository subscribeRepository;
    @Autowired RedisConnectionFactory redisConnectionFactory;

    User user;
    GroupChannel groupChannel1; GroupChannel groupChannel2;
    Chat chat1; Chat chat2;
    SubScribeCommandDto scribeCommandDto1;
    SubScribeCommandDto scribeCommandDto2;

    @BeforeEach
    void setup() {
        user = User.builder().role(Role.USER).userKey("test12343").password("").build();
        userRepository.save(user);
        groupChannel1 = GroupChannel.builder()
                .channelType(ChannelType.GROUP)
                .owner(user)
                .channelLimit(5).build();
        chat1 = Chat.builder().title("").channel(groupChannel1).owner(user).build();
        groupChannel2 = GroupChannel.builder()
                .channelType(ChannelType.GROUP)
                .owner(user)
                .channelLimit(5).build();
        chat2 = Chat.builder().title("").channel(groupChannel2).owner(user).build();
        channelRepository.save(groupChannel1);
        channelRepository.save(groupChannel2);
        chatRepository.save(chat1);
        chatRepository.save(chat2);
        scribeCommandDto1 = SubScribeCommandDto.builder().userId(user.getId()).sessionId("normal").subscriptionId("subscription1")
                .channelId(groupChannel1.getId()).destination("/sub/channel/" + groupChannel1.getId()).build();
        scribeCommandDto2 = SubScribeCommandDto.builder().userId(user.getId()).sessionId("normal").subscriptionId("subscription2")
                .channelId(groupChannel2.getId()).destination("/sub/channel/" + groupChannel2.getId()).build();
    }

    @AfterEach
    void flush() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        connection.flushDb();
        connection.close();
        chatRepository.deleteById(chat1.getId());
        chatRepository.deleteById(chat2.getId());
        channelRepository.deleteById(groupChannel1.getId());
        channelRepository.deleteById(groupChannel2.getId());
        userRepository.deleteById(user.getId());
    }

    @Nested
    @DisplayName("Subscribe 테스트")
    class Subscribe {
        @Test
        @DisplayName("성공")
        void test1(){
            // when
            subscribeService.subscribe(scribeCommandDto1);

            // then
            // 채널에 유저 등록 여부
            Set<Long> userIds = subscribeRepository.findSubscribersByChannelId(scribeCommandDto1.getChannelId());
            assertTrue(userIds.contains(user.getId()));

            // 구독Id 에 채널Id 등록 확인
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(scribeCommandDto1.getSubscriptionId()).or(Assertions::fail);
            assertEquals(channelId.get(), scribeCommandDto1.getChannelId());

            // 세션에 구독 아이디 등록 확인
            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(scribeCommandDto1.getSessionId());
            assertTrue(subscriptionIds.contains(scribeCommandDto1.getSubscriptionId()));
        }
    }

    @Nested
    @DisplayName("Unsubscribe 테스트")
    class Unsubscribe {
        @Test
        @DisplayName("성공")
        void test1(){
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), scribeCommandDto1.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(scribeCommandDto1);

            // when
            subscribeService.unsubscribe(scribeCommandDto1.getSessionId(),
                    scribeCommandDto1.getSubscriptionId());

            // then
            // 채널에 유저 등록 삭제 확인
            Set<Long> userIds = subscribeRepository.findSubscribersByChannelId(scribeCommandDto1.getChannelId());
            assertFalse(userIds.contains(user.getId()));

            // 구독 ID 삭제 확인
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(scribeCommandDto1.getSubscriptionId());
            assertTrue(channelId.isEmpty());

            // 세션에 구독 ID 삭제 확인
            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(scribeCommandDto1.getSessionId());
            assertFalse(subscriptionIds.contains(scribeCommandDto1.getSubscriptionId()));
        }
    }

    @Nested
    @DisplayName("Disconnect 테스트")
    class Disconnect {
        @Test
        @DisplayName("성공")
        void test1() {
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), scribeCommandDto1.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(scribeCommandDto1);
            subscribeService.subscribe(scribeCommandDto2);

            // when
            subscribeService.disconnect(scribeCommandDto1.getSessionId());

            // then
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(scribeCommandDto1.getSubscriptionId());
            assertTrue(channelId.isEmpty());

            Optional<Long> userId = subscribeRepository.findUserIdBySessionId(scribeCommandDto1.getSessionId());
            assertTrue(userId.isEmpty());

            Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(scribeCommandDto1.getChannelId());
            assertFalse(subscribers.contains(user.getId()));

            Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdBySessionId(scribeCommandDto1.getSessionId());
            assertTrue(channelIds.isEmpty());

            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(scribeCommandDto1.getSessionId());
            assertTrue(subscriptionIds.isEmpty());
        }

    }

}