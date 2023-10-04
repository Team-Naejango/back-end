package com.example.naejango.domain.chat.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum MessageType {

    @JsonProperty("SUBSCRIBE_INFO")
    SUBSCRIBE_INFO("웹소켓 채널 정보를 수신합니다.", "/sub/info/"),
    @JsonProperty("SUBSCRIBE_LOUNGE")
    SUBSCRIBE_LOUNGE("라운지 채널 구독을 시작합니다.", "/sub/lounge/"),
    @JsonProperty("SUBSCRIBE_CHANNEL")
    SUBSCRIBE_CHANNEL("채팅 채널 구독을 시작합니다.", "/sub/channel/"),
    @JsonProperty("CHAT")
    CHAT("", "/pub/channel/"),
    @JsonProperty("ENTER")
    ENTER("채널에 입장하였습니다.", ""),
    @JsonProperty("EXIT")
    EXIT("채널에서 퇴장하였습니다.", ""),
    @JsonProperty("OPEN")
    OPEN("채팅이 시작되었습니다.", ""),
    @JsonProperty("CLOSE")
    CLOSE("채팅이 종료되었습니다.", ""),
    @JsonProperty("TRADE")
    TRADE("거래 예약이 등록되었습니다.", ""),
    ;

    private final String defaultMessage;
    private final String endpointPrefix;

    MessageType(String message, String url) {
        defaultMessage = message;
        this.endpointPrefix = url;
    }

}
