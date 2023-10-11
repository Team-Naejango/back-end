package com.example.naejango.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;
    private final StompErrorHandler stompErrorHandler;

    /**
     * stompJs websocket 연결을 위한 EndPoint
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-endpoint")
                .setAllowedOrigins("http://localhost:5500","https://localhost:3000", "https://naejango.site")
                .withSockJS();
        registry.setErrorHandler(stompErrorHandler);
    }

    /*
     * 메시지 브로커 설정
     * enableSimpleBroker 설정을 사용하면 SimpleBrokerMessageHandler 를 메세지 브로커로 사용하는데
     * ConcurrentHashMap 을 이용하여 세션정보를 메모리에 저장해두고 메시지를 구독/발행 한다.
     * Redis 는 STOMP 프로토콜을 지원하지 않기 때문에 서버 자체적으로는 해당 Simple MessageBroker 를 사용하고,
     * 서버 간의 메세지 브로킹에 한해서 적용하게 된다. STOMP 프로토콜을 지원하는 RabbitMQ, ActiveMQ 같은 경우
     * enableStompBrokerRelay 라는 설정으로 외부의 메세지 브로커를 직접 갖다 쓸 수 있는데,
     * 간단한 설정으로 다양한 기술을 사용할 수 있다고 한다.
     * 만약 채팅서비스를 더 고도화 시키고자 한다면 도입해볼 여지가 있지만
     * 본 프로젝트에서는 기능상 더 다양하게 쓰임이 있는 Redis 를 활용하여 메세지 브로커 기능을 수행하도록 한다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor);
    }

}