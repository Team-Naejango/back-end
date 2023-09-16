package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.WebSocketMessageSendDto;
import com.example.naejango.domain.chat.dto.request.WebSocketMessageReceiveDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("Test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
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
    JwtGenerator jwtGenerator;
    @Autowired
    DataSourceProperties dataSourceProperties;

    private final String ENDPOINT = "/ws-endpoint";
    private final String CHAT_CHANNEL = "/sub/channel";
    private final String LOUNGE_CHANNEL = "/sub/lounge";
    private final String INFO_CHANNEL = "/user/sub/info";
    private final String SEND_MESSAGE_CHANNEL = "/pub/channel";
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
    @Order(1)
    @DisplayName("웹소켓 Endpoint 연결 테스트")
    public void testWebSocketConnection() throws Exception {
        // given
        User user1 = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtGenerator.generateAccessToken(user1.getId());
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);

        // when
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // then
        assertNotNull(stompSession);
        assertTrue(stompSession.isConnected());
        stompSession.disconnect();
    }

    @Test
    @Order(2)
    @DisplayName("Info 채널 구독 및 에러메세지 수신")
    void chatChannelSubscribeTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user2 = userRepository.findByUserKey("test_2").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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

        // 권한 없는 채널 구독
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination(CHAT_CHANNEL + "/5");
        stompSession.subscribe(subscribeHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // then
        var dto = new WebSocketMessageReceiveDto(MessageType.INFO, user2.getId(), null, "소켓 통신 정보를 수신합니다.");
        assertEquals(dto.getContent(), objectMapper.readValue(blockingQueue.poll(), WebSocketMessageSendDto.class).getContent());
        assertEquals(objectMapper.writeValueAsString(WebSocketErrorResponse.response(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST))
                , blockingQueue.poll());

        // 종료
        stompSession.disconnect();
    }

    @Test
    @Order(3)
    @DisplayName("라운지 채널만 구독시 전송 실패")
    void sendLoungeChannelTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user4 = userRepository.findByUserKey("test_4").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtGenerator.generateAccessToken(user4.getId());
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
        // when
        // INFO 채널 구독
        StompHeaders subscribeInfoHeaders = new StompHeaders();
        subscribeInfoHeaders.setDestination(INFO_CHANNEL);
        stompSession.subscribe(subscribeInfoHeaders, defaultStompFrameHandler);
        Thread.sleep(200);

        // 라운지 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.setDestination(LOUNGE_CHANNEL + "/2");
        stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 메세지 전송
        StompHeaders sendMessageHeaders = new StompHeaders();
        sendMessageHeaders.setDestination(SEND_MESSAGE_CHANNEL + "/2");
        WebSocketMessageReceiveDto messageDto = WebSocketMessageReceiveDto.builder()
                .messageType(MessageType.CHAT).channelId(2L).senderId(user4.getId()).content("메세지 전송").build();
        stompSession.send(sendMessageHeaders, objectMapper.writeValueAsBytes(messageDto));
        Thread.sleep(100);

        // then
        assertEquals("소켓 통신 정보를 수신합니다.", objectMapper.readValue(blockingQueue.poll(), WebSocketMessageSendDto.class).getContent());
        assertEquals("라운지 채널을 구독 합니다.", objectMapper.readValue(blockingQueue.poll(), WebSocketMessageSendDto.class).getContent());
        assertEquals(objectMapper.writeValueAsString(WebSocketErrorResponse.response(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST))
                , blockingQueue.poll());

        // 종료
        stompSession.disconnect();
    }

    @Test
    @Order(4)
    @DisplayName("채팅 채널에 메세지 전송 및 수신")
    void sendMessageTest() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // given
        User user4 = userRepository.findByUserKey("test_4").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtGenerator.generateAccessToken(user4.getId());
        DefaultStompFrameHandler defaultStompFrameHandler = new DefaultStompFrameHandler();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.set(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + accessToken);
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        // when
        // 채팅 채널 구독
        StompHeaders subscribeChatHeaders = new StompHeaders();
        subscribeChatHeaders.setDestination(CHAT_CHANNEL + "/2");
        stompSession.subscribe(subscribeChatHeaders, defaultStompFrameHandler);
        Thread.sleep(100);

        // 메세지 전송
        StompHeaders sendMessageHeaders = new StompHeaders();
        sendMessageHeaders.setDestination(SEND_MESSAGE_CHANNEL + "/2");
        WebSocketMessageReceiveDto messageDto = WebSocketMessageReceiveDto.builder()
                .messageType(MessageType.CHAT).channelId(2L).senderId(user4.getId()).content("메세지 전송").build();
        stompSession.send(sendMessageHeaders, objectMapper.writeValueAsBytes(messageDto));
        Thread.sleep(100);

        // then
        var dto2 = new WebSocketMessageReceiveDto(MessageType.ENTER, user4.getId(), 2L, "채팅 채널을 구독 합니다.");
        assertEquals(dto2.getContent(), objectMapper.readValue(blockingQueue.poll(), WebSocketMessageSendDto.class).getContent());
        assertEquals(messageDto.getContent(), objectMapper.readValue(blockingQueue.poll(), WebSocketMessageSendDto.class).getContent());

        // 종료
        stompSession.disconnect();
    }




    @SuppressWarnings("ResultOfMethodCallIgnored")
    private class DefaultStompFrameHandler implements StompFrameHandler {
        @NotNull
        @Override
        public Type getPayloadType(@NotNull StompHeaders headers) {
            return byte[].class;
        }
        @Override
        public void handleFrame(@NotNull StompHeaders headers, Object payload) {
            blockingQueue.offer(new String((byte[]) payload));
        }
    }

}
