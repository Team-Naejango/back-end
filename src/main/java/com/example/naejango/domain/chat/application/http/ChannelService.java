package com.example.naejango.domain.chat.application.http;

import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.*;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebSocketService webSocketService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final ItemRepository itemRepository;
    private final EntityManager em;
    private final GeomUtil geomUtil;

    /** 새로운 일대일 채널을 생성합니다. */
    @Transactional
    public CreateChannelDto createPrivateChannel(Long requesterId, Long otherUserId) {
        // 두 유저 사이 진행중인 Chat 을 조회합니다.
        Optional<ChannelAndChatDto> chatDtoOptional = chatRepository.findPrivateChannelBetweenUsers(requesterId, otherUserId);

        // 이미 채팅방이 있는 경우
        if (chatDtoOptional.isPresent()) {
            return new CreateChannelDto(false,
                    chatDtoOptional.get().getChannelId(),
                    chatDtoOptional.get().getChatId());
        }

        // 회원 로드
        User requester = userRepository.findUserWithProfileById(requesterId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User otherUser = userRepository.findUserWithProfileById(otherUserId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 채팅 채널 개설
        PrivateChannel newPrivateChannel = PrivateChannel.builder().channelType(ChannelType.PRIVATE).build();
        channelRepository.save(newPrivateChannel);

        // 채팅방을 생성합니다. 방 제목을 상대방 닉네임으로 설정합니다.
        Chat requesterChat = Chat.builder()
                .owner(requester)
                .channel(newPrivateChannel)
                .title(otherUser.getUserProfile().getNickname())
                .build();

        Chat otherUserChat = Chat.builder()
                .owner(otherUser)
                .channel(newPrivateChannel)
                .title(requester.getUserProfile().getNickname())
                .build();

        chatRepository.save(requesterChat);
        chatRepository.save(otherUserChat);

        // 채널 시작 메세지를 생성합니다.
        messageService.publishMessage(newPrivateChannel.getId(), null, MessageType.INFO, "채팅이 시작 되었습니다.");

        return new CreateChannelDto(true, newPrivateChannel.getId(), requesterChat.getId());
    }

    /** 그룹 채널 생성 */
    @Transactional
    public CreateChannelDto createGroupChannel(Long userId, Long itemId, String defaultTitle, int channelLimit) {
        // 아이템 로드
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // item 이 공동 구매인지 검증
        if(!item.getItemType().equals(ItemType.GROUP_BUY)) {
            throw new CustomException(ErrorCode.CANNOT_GENERATE_GROUP_CHANNEL);
        }

        // 해당 아이템의 주인이 맞는지 검증
        if (!userId.equals(itemRepository.findUserIdById(itemId))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_CREATE_CHANNEL);
        }

        // 해당 아이템에 할당된 채널이 있는지 검증
        Optional<GroupChannel> channelOptional = channelRepository.findGroupChannelByItemId(itemId);

        // 있는 경우
        if (channelOptional.isPresent()) {
            GroupChannel channel = channelOptional.get();
            Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channel.getId(), userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
            return new CreateChannelDto(false, channel.getId(), chat.getId());
        }

        // 그룹 채널 개설
        Channel newGroupChannel = GroupChannel.builder()
                .owner(em.getReference(User.class, userId))
                .item(item)
                .channelType(ChannelType.GROUP)
                .participantsCount(1)
                .defaultTitle(defaultTitle)
                .channelLimit(channelLimit)
                .build();
        channelRepository.save(newGroupChannel);

        // 채팅방을 생성합니다.
        Chat chat = Chat.builder()
                .owner(em.getReference(User.class, userId))
                .channel(newGroupChannel)
                .title(defaultTitle)
                .build();
        chatRepository.save(chat);

        // 채널 시작 메세지를 생성합니다.
        messageService.publishMessage(newGroupChannel.getId(), null, MessageType.INFO, "채팅이 시작되었습니다.");

        return new CreateChannelDto(true, newGroupChannel.getId(), chat.getId());
    }

    /** 공동 구매 아이템에 등록된 그룹 채널 조회 */
    public Optional<GroupChannelDto> findGroupChannel(Long itemId) {
        return channelRepository.findGroupChannelByItemId(itemId).map(GroupChannelDto::new);
    }

    /** 근처의 그룹 채널 조회 */
    public List<GroupChannelDto> findGroupChannelNearby(Coord center, int radius, int page, int size) {
        Point centerPoint = geomUtil.createPoint(center);
        Page<GroupChannel> findResult = channelRepository.findGroupChannelNearBy(centerPoint, radius, PageRequest.of(page, size));
        return findResult.getContent().stream().map(GroupChannelDto::new).collect(Collectors.toList());
    }

    /** 채널의 참여 인원 정보 조회 */
    public List<ParticipantInfoDto> findParticipantsInChannel(Long channelId, Long userId) {
        // 조회
        List<User> participants = channelRepository.findParticipantsByChannelId(channelId);

        // 조회 권한 확인
        if(participants.stream().filter(p -> p.getId().equals(userId)).findAny().isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);

        return participants.stream().map(participant -> new ParticipantInfoDto(participant.getId(),
                participant.getUserProfile().getNickname(), participant.getUserProfile().getImgUrl())).collect(Collectors.toList());
    }

    /** 채널 퇴장 */
    @Transactional
    public void deleteChat(Long channelId, Long userId) {
        // Chat 로드
        Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        // 권한 확인
        if(chat.getOwner().getId().equals(userId)) throw new CustomException(ErrorCode.CHAT_NOT_FOUND);

        // Channel 로드
        Channel channel = channelRepository.findByChatId(chat.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // Chat 에 연관된 ChatMessage 를 삭제합니다.
        chatMessageRepository.deleteChatMessageByChatId(chat.getId());

        // 일대일 채널의 경우
        if (channel.getChannelType().equals(ChannelType.PRIVATE)) {
            // 상대방이 Chat 이 없거나 ChatMessage 가 없는지 확인
            Optional<Chat> otherChat = chatRepository.findOtherChatByPrivateChannelId(channel.getId(), chat.getId());
            if (otherChat.isEmpty() || !chatMessageRepository.existsByChatId(otherChat.get().getId())) {
                // Chat 삭제
                chatRepository.delete(chat);
                // Channel 의 모든 message 삭제
                messageRepository.deleteMessagesByChannelId(channel.getId());
                // Channel 삭제
                channelRepository.deleteById(channel.getId());
            }
        }

        // 그룹 채팅의 경우
        if (channel.getChannelType().equals(ChannelType.GROUP)) {
            // 그룹 채널로 캐스팅 합니다.
            GroupChannel groupChannel = (GroupChannel) channel;
            // 퇴장 메세지 발행
            WebSocketMessageDto messageDto = WebSocketMessageDto.builder()
                    .messageType(MessageType.EXIT)
                    .content("채널에서 퇴장 하였습니다.")
                    .channelId(groupChannel.getId())
                    .userId(userId).build();
            webSocketService.publishMessage(String.valueOf(groupChannel.getId()), messageDto);
            // Chat 을 삭제합니다. (더이상 메세지를 수신하지 못하도록)
            chatRepository.delete(chat);
            // Channel 의 참여자 수를 줄입니다.
            groupChannel.decreaseParticipantCount();
            // 만약 채널의 참여자가 0 이 되면 연관된 메세지와 채널을 삭제합니다.
            if (groupChannel.getParticipantsCount() == 0) {
                messageRepository.deleteMessagesByChannelId(channel.getId());
                channelRepository.deleteById(channel.getId());
            }
        }
    }

}
