package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class WebSocketTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    ObjectMapper objectMapper;
    private final String ENDPOINT = "/ws-endpoint";
    private final String TEST_TOPIC = "/topic/chat/1";
    private final String TEST_MESSAGE = "Hello, WebSocket!";

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<String> blockingQueue;

    @BeforeEach
    public void setup() {
        this.stompClient = new WebSocketStompClient(new SockJsClient(
                Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))));
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
    @DisplayName("채팅채널 Subscribe 테스트")
    public void testWebSocketSubscription() throws Exception {
        stompSession = stompClient.connect("ws://localhost:8080" + ENDPOINT, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
        stompSession.subscribe(TEST_TOPIC, new DefaultStompFrameHandler());

        // 메시지 보내기
        stompSession.send(TEST_TOPIC, TEST_MESSAGE.getBytes());

        // 구독을 통해 메시지 수신 확인
        String receivedMessage = blockingQueue.poll(1, SECONDS);
        assertEquals(TEST_MESSAGE, receivedMessage);
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
