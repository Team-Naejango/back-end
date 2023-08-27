package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.dto.SendMessageResponseDto;
import com.example.naejango.domain.chat.dto.SubscribeResponseDto;
import com.example.naejango.domain.chat.repository.*;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("Test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class WebSocketTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    ChannelUserRepository channelUserRepository;
    @Autowired
    JwtGenerator jwtGenerator;
    @Autowired
    DataSourceProperties dataSourceProperties;

    private final String ENDPOINT = "/ws-endpoint";
    private final String TEST_SUBSCRIBE = "/topic/channel";
    private final String TEST_SEND = "/chat/channel";
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<String> blockingQueue;

    @BeforeEach
    public void setup() {
        this.stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @Test
    @DisplayName("웹소켓 Endpoint 연결 테스트")
    public void testWebSocketConnection() throws Exception {
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
        assertNotNull(stompSession);
        assertTrue(stompSession.isConnected());
    }
    @Test
    @DisplayName("채널 Subscribe 테스트 ")
    void test3() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
        User user1 = userRepository.findByUserKey("test_1").get();
        String accessToken = jwtGenerator.generateAccessToken(user1.getId());

        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        subscribeHeaders.setDestination(TEST_SUBSCRIBE +"/1");
        // when
        stompSession.subscribe(subscribeHeaders, new DefaultStompFrameHandler());
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(1L, 1L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
    }

    @Test
    @DisplayName("메세지 send 테스트")
    void test4() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        stompSession = stompClient.connect("http://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        User user2 = userRepository.findByUserKey("test_2").get();
        String accessToken = jwtGenerator.generateAccessToken(user2.getId());

        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        subscribeHeaders.setDestination(TEST_SUBSCRIBE +"/1");
        stompSession.subscribe(subscribeHeaders, new DefaultStompFrameHandler());
        Thread.sleep(200);

        // when
        StompHeaders messageHeaders = new StompHeaders();
        messageHeaders.setDestination(TEST_SEND + "/1");
        messageHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);

        var requestDto1 = SendMessageResponseDto.builder().senderId(user2.getId()).content("테스트 메세지").channelId(1L).build();
        stompSession.send(messageHeaders, objectMapper.writeValueAsBytes(requestDto1));
        Thread.sleep(100);

        // then
        var dto1 = new SubscribeResponseDto(2L, 1L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto1), blockingQueue.poll(1, SECONDS));
        var dto2 = new SendMessageResponseDto(2L, 1L, "테스트 메세지"); // 보낸 메세지가 구독 되었던 채널로 돌아옴
        assertEquals(objectMapper.writeValueAsString(dto2), blockingQueue.poll(1, SECONDS));
    }

    private class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return byte[].class;
        }
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            blockingQueue.offer(new String((byte[]) payload));
        }
    }

}
