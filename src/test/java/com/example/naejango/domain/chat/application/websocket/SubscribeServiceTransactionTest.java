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
import com.example.naejango.global.common.exception.TestException;
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
@ActiveProfiles({"TestStub:AspectJ", "test"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SubscribeServiceTransactionTest {
    @Autowired SubscribeService subscribeService;
    @Autowired UserRepository userRepository;
    @Autowired ChatRepository chatRepository;
    @Autowired ChannelRepository channelRepository;
    @Autowired SubscribeRepository subscribeRepository;
    @Autowired RedisConnectionFactory redisConnectionFactory;

    User user;
    GroupChannel groupChannel1; GroupChannel groupChannel2;
    Chat chat1; Chat chat2;
    SubScribeCommandDto errorCommandForSubscribe;
    SubScribeCommandDto errorCommandForUnsubscribe;
    SubScribeCommandDto errorCommandForDisconnect;

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
        errorCommandForSubscribe = SubScribeCommandDto.builder().userId(user.getId()).sessionId("subscribe").subscriptionId("subscription")
                .channelId(groupChannel1.getId()).destination("/sub/channel/" + groupChannel1.getId()).build();
        errorCommandForUnsubscribe = SubScribeCommandDto.builder().userId(user.getId()).sessionId("unsubscribe").subscriptionId("subscription")
                .channelId(groupChannel1.getId()).destination("/sub/channel/" + groupChannel1.getId()).build();
        errorCommandForDisconnect = SubScribeCommandDto.builder().userId(user.getId()).sessionId("disconnect").subscriptionId("subscription")
                .channelId(groupChannel1.getId()).destination("/sub/channel/" + groupChannel1.getId()).build();
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
    @DisplayName("예외 발생시 Transaction 롤백 테스트")
    class TransactionTest {
        @Test
        @DisplayName("subscribe")
        void test1() {
            // when
            assertThrows(TestException.class, () ->
                    subscribeService.subscribe(errorCommandForSubscribe)
            );

            // then : 에러가 발생하기 이전의 로직 롤백 확인
            // 구독 정보 저장 롤백 확인
            Optional<Long> channelId = subscribeRepository
                    .findChannelIdBySubscriptionId(errorCommandForSubscribe.getSubscriptionId());
            assertTrue(channelId.isEmpty());
            // 채널 멤버 정보 롤백 확인
            Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(errorCommandForSubscribe.getChannelId());
            assertTrue(subscribers.isEmpty());
        }
        @Test
        @DisplayName("Unsubscribe")
        void test2(){
            // given
            subscribeService.subscribe(errorCommandForUnsubscribe);
            subscribeRepository.saveUserIdBySessionId(user.getId(), errorCommandForUnsubscribe.getSessionId());

            // when
            assertThrows(TestException.class,
                    () -> subscribeService.unsubscribe(errorCommandForUnsubscribe.getSessionId()
                            , errorCommandForUnsubscribe.getSubscriptionId())
            );

            // then : 에러가 발생하기 이전의 로직 롤백 확인
            // 구독 아이디 삭제 롤백ㅌㅌ
            Optional<Long> channelId = subscribeRepository
                    .findChannelIdBySubscriptionId(errorCommandForSubscribe.getSubscriptionId());
            assertTrue(channelId.isPresent());
        }


        @Test
        @DisplayName("Disconnect")
        void test3() {
            // given
            subscribeRepository.saveUserIdBySessionId(user.getId(), errorCommandForDisconnect.getSessionId()); // 웹소켓 접속
            subscribeService.subscribe(errorCommandForDisconnect);

            // when
            assertThrows(TestException.class,
                    () -> subscribeService.disconnect(errorCommandForDisconnect.getSessionId())
            );

            // then
            assertFalse(subscribeRepository
                    .findSubscriptionIdBySessionId(errorCommandForDisconnect.getSessionId()).isEmpty()
            );
        }

    }

}
