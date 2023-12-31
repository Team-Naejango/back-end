package com.example.naejango.domain.chat.application.http;

import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.chat.dto.MessagePublishCommandDto;
import com.example.naejango.domain.chat.repository.*;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.dto.NotificationPublishDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SubscribeRepository subscribeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void publishMessage(MessagePublishCommandDto commandDto) {
        // 메세지를 저장합니다.
        Channel channel = channelRepository.findById(commandDto.getChannelId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // 채널 상태 확인
        if (channel.getIsClosed() && !commandDto.getMessageType().equals(MessageType.EXIT)) {
            throw new CustomException(ErrorCode.CHANNEL_IS_CLOSED);
        }

        Message sentMessage = commandDto.toEntity(channel);
        messageRepository.save(sentMessage);

        // 메세지를 채팅방에 할당 합니다.
        // 현재 메시지를 구독 중인(보고 있는) 구독자를 찾아옵니다.
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channel.getId());
        // 채널에 연결되어 있는 모든 chatId를 찾아 옵니다.
        chatRepository.findByChannelId(channel.getId()).forEach(chat -> {
            ChatMessage chatMessage = ChatMessage.builder().isRead(false).message(sentMessage).chat(chat).build();
            // 현재 구독중인(보고 있는) 유저의 경우 읽음 처리를 합니다.
            if(subscribers.contains(chat.getOwner().getId())) chatMessage.read();
            else {
                // 보고 있지 않은 사람들은 알림을 보내줍니다.
                eventPublisher.publishEvent(new NotificationPublishDto(
                        chat.getOwner().getId(),
                        NotificationType.CHAT,
                        commandDto.getContent(),
                        String.valueOf(commandDto.getChannelId())
                ));
            }
            chatMessageRepository.save(chatMessage);
        });

        // Channel 의 마지막 메세지를 업데이트 합니다.
        channel.updateLastMessage(commandDto.getContent());
    }

    @Transactional
    public List<MessageDto> recentMessages(Long userId, Long chatId, int page, int size) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        // 권한 확인
        if(!userId.equals(chat.getOwner().getId())) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);

        // 조회
        Page<Message> findResult = messageRepository.findRecentMessages(chatId, PageRequest.of(page, size));

        // 예외 처리 - Message 가 0 개 인 경우는 없음(채널 시작시 메세지 생성)
        if(findResult.isEmpty()) throw new CustomException(ErrorCode.MESSAGE_NOT_FOUND);

        // 메세지 읽기
        chatMessageRepository.readMessage(chatId);

        return findResult.getContent().stream().map(MessageDto::new).collect(Collectors.toList());
    }

}
