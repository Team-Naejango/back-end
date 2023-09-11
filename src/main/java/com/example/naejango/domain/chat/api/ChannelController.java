package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.request.FindGroupChannelNearbyRequestDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChannelRequestDto;
import com.example.naejango.domain.chat.dto.response.FindChannelParticipantsResponseDto;
import com.example.naejango.domain.chat.dto.response.FindGroupChannelNearbyResponseDto;
import com.example.naejango.domain.chat.dto.response.StartGroupChannelResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChannelResponseDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel")
public class ChannelController {

    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final ChannelService channelService;
    private final ChannelRepository channelRepository;
    private final ItemRepository itemRepository;
    private final GeomUtil geomUtil;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 특정 회원과의 Private Channel 을 개설합니다.
     * 만약 이미 채팅방이 존재한다면 해당 채팅방의 채널 id 및 요청자의 chatId 값을 반환하고
     * 존재하지 않으면 채널을 생성한 뒤 해당 값을 반환 합니다.
     * @param otherUserId 상대방 id
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId), 생성 결과(message)
     */
    @PostMapping ("/private/{otherUserId}")
    public ResponseEntity<StartPrivateChannelResponseDto> startPrivateChannel(@PathVariable("otherUserId") Long otherUserId,
                                                                              Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 두 유저 사이 진행중인 Chat 을 조회합니다.
        Optional<ChannelAndChatDto> chatDtoOptional = chatRepository.findPrivateChannelBetweenUsers(userId, otherUserId);

        // 이미 채팅방이 있는 경우
        if(chatDtoOptional.isPresent()) {
            ChannelAndChatDto dto = chatDtoOptional.get();
            return ResponseEntity.status(HttpStatus.CONFLICT) // 이미 진행중인 채팅이 있음을 나타내는 응답 코드
                    .body(new StartPrivateChannelResponseDto(dto.getChannelId(), dto.getChatId(), "이미 진행중인 채널이 있습니다."));
        }

        // 채팅방 생성
        ChannelAndChatDto dto = chatService.createPrivateChannel(userId, otherUserId);
        return ResponseEntity.status(HttpStatus.CREATED) // 채팅방이 생성 되었음을 나타내는 응답 코드
                .body(new StartPrivateChannelResponseDto(dto.getChannelId(), dto.getChatId(), "일대일 채널이 개설되었습니다."));
    }

    /**
     * 그룹 채널을 개설합니다.
     * 그룹 채널은 ItemsStorage 에 종속된 것으로, 특정 Storage 에서 조회하여 해당 item 에 할당된 그룹 채널에 접근 가능합니다.
     * 해당 item 이 등록된 storage 의 위치 정보를 통해 주변의 그룹 채널 검색이 가능합니다.
     * @param requestDto 창고 id (storageId), 기본 채널 제목(defaultTitle), 방 정원(limit)
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId)
     */
    @PostMapping("/group")
    public ResponseEntity<StartGroupChannelResponseDto> startGroupChannel(@RequestBody StartGroupChannelRequestDto requestDto,
                                                                          Authentication authentication) {

        Long userId = authenticationHandler.getUserId(authentication);
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // item 이 공동 구매인지 검증
        if(!item.getItemType().equals(ItemType.GROUP_BUY)) {
            throw new CustomException(ErrorCode.CANNOT_GENERATE_GROUP_CHANNEL);
        }

        // 해당 아이템의 주인이 맞는지 검증
        if (!userId.equals(itemRepository.findUserIdById(requestDto.getItemId()))) throw new CustomException(ErrorCode.UNAUTHORIZED_CREATE_CHANNEL);

        // 해당 아이템에 할당된 채널이 있는지 검증
        Optional<GroupChannel> channelOptional = channelRepository.findGroupChannelByItemId(requestDto.getItemId());
        if (channelOptional.isPresent()) {
            GroupChannel channel = channelOptional.get();
            Chat chat = chatRepository.findChatByChannelIdAndOwnerId(channel.getId(), userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new StartGroupChannelResponseDto(channel.getId(), chat.getId(), "이미 진행중인 채널이 있습니다."));
        }

        // 그룹 채널 생성
        var result = chatService.createGroupChannel(userId, requestDto.getItemId(), requestDto.getDefaultTitle(), requestDto.getLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(new StartGroupChannelResponseDto(result.getChannelId(), result.getChatId(), "그룹 채널이 개설되었습니다."));
    }

    /**
     * 근처에 있는 GroupChannel 을 조회합니다.
     * @param requestDto 경도(lon) 위도(lat), 반경(rad)
     * @return 근처 그룹 채널 정보
     */
    @GetMapping("/group/nearby")
    public ResponseEntity<FindGroupChannelNearbyResponseDto> findGroupChannelNearby(@Valid @ModelAttribute FindGroupChannelNearbyRequestDto requestDto) {
        // 요청 정보 확인
        Coord centerCoord = new Coord(requestDto.getLon(), requestDto.getLat());
        Point centerPoint = geomUtil.createPoint(centerCoord);

        // 채널을 조회합니다.
        Page<GroupChannel> channels = channelRepository.findGroupChannelNearBy(centerPoint, requestDto.getRad(), PageRequest.of(0, 10));

        // 조회 결과가 없는 경우
        if (channels.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new FindGroupChannelNearbyResponseDto("근처에 진행중인 그룹 채팅이 없습니다.", centerCoord, requestDto.getRad(), null));
        }

        // 조회 결과가 있는 경우
        List<GroupChannelDto> result = channels.getContent().stream().map(GroupChannelDto::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(new FindGroupChannelNearbyResponseDto("가까운 그룹 채널이 조회 되었습니다", centerCoord, requestDto.getRad(), result));
    }

    /**
     * 채팅 채널(channel)에 참여중인 유저의 정보를 조회합니다.
     * @param channelId 조회하고자 하는 채널 id
     * @return 총 참여자(total)
     *         참여자 정보(List<ParticipantInfoDto>) :
     *         참여자 id(participantId), 닉네임(nickname), 이미지링크(imgUrl)
     */
    @GetMapping("/{channelId}/participants")
    public ResponseEntity<FindChannelParticipantsResponseDto> findChannelParticipants(@PathVariable("channelId") Long channelId,
                                                                                      Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        List<ParticipantInfoDto> result = channelService.findParticipantsInChannel(channelId);

        // 권한 확인
        if(result.stream().filter(p -> p.getParticipantId().equals(userId)).findAny().isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);

        return ResponseEntity.ok().body(new FindChannelParticipantsResponseDto("채널 참여자 정보를 조회하였습니다.",
                result.size(), result));
    }
}
