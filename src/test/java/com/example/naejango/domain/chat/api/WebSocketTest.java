package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.dto.SubscribeResponseDto;
import com.example.naejango.domain.chat.dto.response.UnsubscribeResponseDto;
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
    private final String CHAT_CHANNEL = "/topic/channel";
    private final String INFO_CHANNEL = "/user/queue/info";
    private final String SEND_MESSAGE_CHANNEL = "/chat/channel";
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
    @DisplayName("Info 채널 구독 테스트 ")
    void info() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        StompHeaders subscribeHeaders = new StompHeaders();
        User user1 = userRepository.findByUserKey("test_1").get();
        String accessToken = jwtGenerator.generateAccessToken(user1.getId());
        subscribeHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        subscribeHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeHeaders, new DefaultStompFrameHandler());
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(1L, null, "소켓 통신 정보를 수신합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
    }

    @Test
    @DisplayName("채팅 채널 구독 - 구독 취소 테스트")
    void chatChannelSubscribeTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        User user1 = userRepository.findByUserKey("test_1").get();
        String accessToken = jwtGenerator.generateAccessToken(user1.getId());
        System.out.println("accessToken = " + accessToken);

        // info 채널 구독
        StompHeaders subscribeInfoHeaders = new StompHeaders();
        subscribeInfoHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        subscribeInfoHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeInfoHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 채팅 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.add("Authorization", JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        subscribeChatHeaders.setDestination(CHAT_CHANNEL + "/1");
        StompSession.Subscription subscription = stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(200);

        // 구독 취소
        subscription.unsubscribe();
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(1L, null, "소켓 통신 정보를 수신합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
        var dto2 = new SubscribeResponseDto(1L, 1L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto2), blockingQueue.poll());
        var dto3 = new UnsubscribeResponseDto(1L, 1L, "채팅 채널 구독이 취소 되었습니다.");
        assertEquals(objectMapper.writeValueAsString(dto3), blockingQueue.poll());

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
