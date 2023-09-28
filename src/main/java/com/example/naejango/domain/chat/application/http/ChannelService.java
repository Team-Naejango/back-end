package com.example.naejango.domain.chat.application.http;

import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.*;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
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
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final WebSocketService webSocketService;
    private final ItemRepository itemRepository;
    private final EntityManager em;
    private final GeomUtil geomUtil;

    /** 새로운 일대일 채널을 생성합니다. */
    @Transactional
    public CreateChannelDto createPrivateChannel(Long requesterId, Long otherUserId) {
        // 동일한 유저인지 확인힙니다.
        if(requesterId.equals(otherUserId)) throw new CustomException(ErrorCode.BAD_REQUEST);

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
        PrivateChannel newPrivateChannel = PrivateChannel.builder().channelType(ChannelType.PRIVATE).isClosed(false).build();
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
        WebSocketMessageCommandDto commandDto = WebSocketMessageCommandDto.builder()
                .messageType(MessageType.OPEN)
                .channelId(newPrivateChannel.getId())
                .senderId(null)
                .content("채팅이 시작 되었습니다.").build();

        // 메세지 저장
        messageService.publishMessage(commandDto);

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
        if (!userId.equals(item.getUser().getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_CREATE_CHANNEL);
        }

        // 해당 아이템에 할당된 채널이 있는지 검증
        Optional<GroupChannel> channelOptional = findGroupChannelByItemId(itemId);

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
                .isClosed(false)
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
        WebSocketMessageCommandDto commandDto = WebSocketMessageCommandDto.builder()
                .messageType(MessageType.OPEN)
                .channelId(newGroupChannel.getId())
                .senderId(null)
                .content("채팅이 시작 되었습니다.").build();

        // 메세지 저장
        messageService.publishMessage(commandDto);

        return new CreateChannelDto(true, newGroupChannel.getId(), chat.getId());
    }

    /** 공동 구매 아이템에 등록된 그룹 채널 조회 */
    public Optional<GroupChannelDto> findGroupChannel(Long itemId) {
        return findGroupChannelByItemId(itemId).map(GroupChannelDto::new);
    }

    public Optional<GroupChannel> findGroupChannelByItemId(Long itemId) {
        return channelRepository.findGroupChannelByItemId(itemId);
    }

    /** 근처의 그룹 채널 조회 */
    public List<GroupChannelDto> findGroupChannelNearby(Coord center, int radius, int page, int size) {
        Point centerPoint = geomUtil.createPoint(center);

        // 종료된 채널은 조회하지 않습니다.
        Page<GroupChannel> findResult = channelRepository.findGroupChannelWithItemNearBy(centerPoint, radius, PageRequest.of(page, size));

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

    /** 채널 종료 */
    @Transactional
    public void closeChannelById(Long channelId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        closeChannel(channel, userId);
    }

    @Transactional
    public void closeChannelByItemId(Long itemId, Long userId) {
        Channel channel = channelRepository.findGroupChannelByItemId(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        closeChannel(channel, userId);
    }

    private void closeChannel(Channel channel, Long userId) {
        // 그룹 채널인 경우
        if (channel.getChannelType().equals(ChannelType.GROUP)) {
            // 방장인지 확인 합니다.
            GroupChannel group = (GroupChannel) channel;
            if(!group.getOwner().getId().equals(userId)) throw new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST);

            // 종료 메세지 전송
            sendCloseMessage(channel.getId(), userId);

            // 채팅 종료
            group.closeChannel();
            return;
        }

        // 일대일 채널인 경우
        else if (channel.getChannelType().equals(ChannelType.PRIVATE)) {
            // 종료 메세지 전송
            sendCloseMessage(channel.getId(), userId);
            // 채팅 종료
            channel.closeChannel();
            return;
        }
        throw new CustomException(ErrorCode.CHANNEL_NOT_FOUND);
    }

    public void sendCloseMessage(Long channelId, Long userId) {
        WebSocketMessageCommandDto commandDto = new WebSocketMessageCommandDto(MessageType.CLOSE, userId, channelId);
        messageService.publishMessage(commandDto);
        webSocketService.publishMessage(commandDto);
    }
}
