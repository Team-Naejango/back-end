package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.dto.request.SendMessageRequestDto;
import com.example.naejango.domain.chat.dto.response.SendMessageResponseDto;
import com.example.naejango.domain.chat.dto.response.SubscribeResponseDto;
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
import org.springframework.web.socket.WebSocketHttpHeaders;
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

@SuppressWarnings("ALL")
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
        // given
        User user1 = userRepository.findByUserKey("test_1").get();
        String accessToken = jwtGenerator.generateAccessToken(user1.getId());

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        System.out.println("accessToken = " + accessToken);

        // when
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // then
        assertNotNull(stompSession);
        assertTrue(stompSession.isConnected());
    }

    @Test
    @DisplayName("Info 채널 및 채팅 채널 구독 테스트")
    void chatChannelSubscribeTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user2 = userRepository.findByUserKey("test_2").get();
        String accessToken = jwtGenerator.generateAccessToken(user2.getId());
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        // info 채널 구독
        StompHeaders subscribeInfoHeaders = new StompHeaders();
        subscribeInfoHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeInfoHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 채팅 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.setDestination(CHAT_CHANNEL + "/2");
        stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(2L, null, "소켓 통신 정보를 수신합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
        var dto2 = new SubscribeResponseDto(2L, 2L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto2), blockingQueue.poll());
    }

    @Test
    @DisplayName("연결 종료 테스트")
    void disconnectTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user3 = userRepository.findByUserKey("test_3").get();
        String accessToken = jwtGenerator.generateAccessToken(user3.getId());
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        // info 채널 구독
        StompHeaders subscribeInfoHeaders = new StompHeaders();
        subscribeInfoHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeInfoHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 채팅 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.setDestination(CHAT_CHANNEL + "/2");
        stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(3L, null, "소켓 통신 정보를 수신합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
        var dto2 = new SubscribeResponseDto(3L, 2L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto2), blockingQueue.poll());
    }

    @Test
    @DisplayName("메세지 전송 테스트")
    void sendMessageTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user4 = userRepository.findByUserKey("test_4").get();
        String accessToken = jwtGenerator.generateAccessToken(user4.getId());
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        // info 채널 구독
        StompHeaders subscribeInfoHeaders = new StompHeaders();
        subscribeInfoHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeInfoHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 채팅 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.setDestination(CHAT_CHANNEL + "/2");
        stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 메세지 전송
        StompHeaders sendMessageHeaders = new StompHeaders();
        sendMessageHeaders.setDestination(SEND_MESSAGE_CHANNEL + "/2");
        SendMessageRequestDto requestDto = SendMessageRequestDto.builder().content("메세지 전송").build();
        stompSession.send(sendMessageHeaders, objectMapper.writeValueAsBytes(requestDto));
        Thread.sleep(100);

        // then
        var dto = new SubscribeResponseDto(4L, null, "소켓 통신 정보를 수신합니다.");
        assertEquals(objectMapper.writeValueAsString(dto), blockingQueue.poll());
        var dto2 = new SubscribeResponseDto(4L, 2L, "채팅 채널 구독을 시작합니다.");
        assertEquals(objectMapper.writeValueAsString(dto2), blockingQueue.poll());
        var dto3 = new SendMessageResponseDto(4L, 2L, "메세지 전송");
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
