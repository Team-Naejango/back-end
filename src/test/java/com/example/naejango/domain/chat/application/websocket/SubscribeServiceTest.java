package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.SubScribeCommandDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.WebSocketException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
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
    SubScribeCommandDto normalSubscribeCommand1;
    SubScribeCommandDto normalSubscribeCommand2;
    SubScribeCommandDto errorCommandForSubscribe;
    SubScribeCommandDto errorCommandForUnsubscribe;
    SubScribeCommandDto errorCommandForDisconnect;

    @BeforeEach
    void setup(){
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
        normalSubscribeCommand1 = SubScribeCommandDto.builder()
                .userId(user.getId())
                .sessionId("normal")
                .subscriptionId("subscription1")
                .channelId(groupChannel1.getId())
                .destination("/sub/channel/" + groupChannel1.getId())
                .build();

        normalSubscribeCommand2 = SubScribeCommandDto.builder()
                .userId(user.getId())
                .sessionId("normal")
                .subscriptionId("subscription2")
                .channelId(groupChannel2.getId())
                .destination("/sub/channel/" + groupChannel2.getId())
                .build();

        errorCommandForSubscribe = SubScribeCommandDto.builder()
                .userId(user.getId())
                .sessionId("subscribe")
                .subscriptionId("subscription")
                .channelId(groupChannel1.getId())
                .destination("/sub/channel/" + groupChannel1.getId())
                .build();

        errorCommandForUnsubscribe = SubScribeCommandDto.builder()
                .userId(user.getId())
                .sessionId("unsubscribe")
                .subscriptionId("subscription")
                .channelId(groupChannel1.getId())
                .destination("/sub/channel/" + groupChannel1.getId())
                .build();

        errorCommandForDisconnect = SubScribeCommandDto.builder()
                .userId(user.getId())
                .sessionId("disconnect")
                .subscriptionId("subscription")
                .channelId(groupChannel1.getId())
                .destination("/sub/channel/" + groupChannel1.getId())
                .build();
    }

    @AfterEach
    @Transactional
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
        @DisplayName("정상 테스트")
        void test1(){
            // when
            subscribeService.subscribe(normalSubscribeCommand1);

            // then
            // 채널에 유저 등록 여부
            Set<Long> userIds = subscribeRepository.findSubscribersByChannelId(normalSubscribeCommand1.getChannelId());
            assertTrue(userIds.contains(user.getId()));

            // 구독Id 에 채널Id 등록 확인
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(normalSubscribeCommand1.getSubscriptionId()).or(Assertions::fail);
            assertEquals(channelId.get(), normalSubscribeCommand1.getChannelId());

            // 세션에 구독 아이디 등록 확인
            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(normalSubscribeCommand1.getSessionId());
            assertTrue(subscriptionIds.contains(normalSubscribeCommand1.getSubscriptionId()));
        }

        @Test
        @Disabled
        @DisplayName("예외 발생시 Transaction 롤백 테스트")
        void test2() {
            // when
            assertThrows(WebSocketException.class, () ->
                    subscribeService.subscribe(errorCommandForSubscribe)
            );

            // then : 에러가 발생하기 이전의 로직 롤백 확인
            // 구독 정보 저장 롤백 확인
            Optional<Long> channelId = subscribeRepository
                    .findChannelIdBySubscriptionId(errorCommandForSubscribe.getSubscriptionId());
            assertTrue(channelId.isEmpty());
        }

    }

    @Nested
    @DisplayName("Unsubscribe 테스트")
    class Unsubscribe {
        @Test
        @DisplayName("정상 테스트")
        void test1(){
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), normalSubscribeCommand1.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(normalSubscribeCommand1);

            // when
            subscribeService.unsubscribe(normalSubscribeCommand1.getSessionId(),
                    normalSubscribeCommand1.getSubscriptionId());

            // then
            // 채널에 유저 등록 삭제 확인
            Set<Long> userIds = subscribeRepository.findSubscribersByChannelId(normalSubscribeCommand1.getChannelId());
            assertFalse(userIds.contains(user.getId()));

            // 구독 ID 삭제 확인
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(normalSubscribeCommand1.getSubscriptionId());
            assertTrue(channelId.isEmpty());

            // 세션에 구독 ID 삭제 확인
            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(normalSubscribeCommand1.getSessionId());
            assertFalse(subscriptionIds.contains(normalSubscribeCommand1.getSubscriptionId()));
        }

        @Test
        @Disabled
        @DisplayName("예외 발생시 Transaction 롤백 테스트")
        void test2(){
            // when
            subscribeService.subscribe(errorCommandForUnsubscribe);
            subscribeRepository.saveUserIdBySessionId(user.getId(), errorCommandForUnsubscribe.getSessionId());
            assertThrows(WebSocketException.class,
                    () -> subscribeService.unsubscribe(errorCommandForUnsubscribe.getSessionId()
                            , errorCommandForUnsubscribe.getSubscriptionId())
            );

            // then : 에러가 발생하기 이전의 로직 롤백 확인
            // 구독 아이디 삭제 롤백
            Optional<Long> channelId = subscribeRepository
                    .findChannelIdBySubscriptionId(errorCommandForSubscribe.getSubscriptionId());
            assertTrue(channelId.isPresent());
        }
    }

    @Nested
    @DisplayName("Disconnect 테스트")
    class Disconnect {
        @Test
        @DisplayName("성공 테스트")
        void test1() {
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), normalSubscribeCommand1.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(normalSubscribeCommand1);
            subscribeService.subscribe(normalSubscribeCommand2);

            // when
            subscribeService.disconnect(normalSubscribeCommand1.getSessionId());

            // then
            Optional<Long> channelId = subscribeRepository.findChannelIdBySubscriptionId(normalSubscribeCommand1.getSubscriptionId());
            assertTrue(channelId.isEmpty());

            Optional<Long> userId = subscribeRepository.findUserIdBySessionId(normalSubscribeCommand1.getSessionId());
            assertTrue(userId.isEmpty());

            Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(normalSubscribeCommand1.getChannelId());
            assertFalse(subscribers.contains(user.getId()));

            Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdBySessionId(normalSubscribeCommand1.getSessionId());
            assertTrue(channelIds.isEmpty());

            Set<String> subscriptionIds = subscribeRepository.findSubscriptionIdBySessionId(normalSubscribeCommand1.getSessionId());
            assertTrue(subscriptionIds.isEmpty());
        }

        @Test
        @Disabled
        @DisplayName("예외 발생시 Transaction 롤백 테스트")
        void test2() {
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), errorCommandForDisconnect.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(errorCommandForDisconnect);

            // when
            assertThrows(WebSocketException.class,
                    () -> subscribeService.disconnect(errorCommandForDisconnect.getSessionId())
            );

            // then
            assertFalse(subscribeRepository
                    .findSubscriptionIdBySessionId(errorCommandForDisconnect.getSessionId()).isEmpty()
            );
        }

    }

}