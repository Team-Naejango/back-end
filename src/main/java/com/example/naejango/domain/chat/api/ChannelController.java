package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.dto.request.FindGroupChannelNearbyRequestDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChannelRequestDto;
import com.example.naejango.domain.chat.dto.response.FindChannelParticipantsResponseDto;
import com.example.naejango.domain.chat.dto.response.FindGroupChannelNearbyResponseDto;
import com.example.naejango.domain.chat.dto.response.StartGroupChannelResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChannelResponseDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
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
    private final GeomUtil geomUtil;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 특정 회원과의 Private Chat 을 시작합니다.
     * 만약 이미 채팅방이 존재한다면 해당 채팅방의 채널 id 값을 반환하고
     * 존재하지 않으면 생성한 뒤 채널 id 값을 반환합니다.
     * @param otherUserId 상대방 ㅑid
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId), 생성 결과(message)
     */
    @PostMapping ("/private/{otherUserId}")
    public ResponseEntity<StartPrivateChannelResponseDto> startPrivateChannel(@PathVariable("otherUserId") Long otherUserId,
                                                                              Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        Optional<PrivateChatDto> result = chatRepository.findPrivateChannelBetweenUsers(userId, otherUserId);

        if (result.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 이미 진행중인 채팅이 있음을 나타내는 응답 코드
                    .body(new StartPrivateChannelResponseDto(result.get().getChannelId(),
                            result.get().getChatId(), "이미 진행중인 채팅이 있습니다."));
        } else {
            PrivateChatDto dto = chatService.createPrivateChannel(userId, otherUserId);
            return ResponseEntity.status(HttpStatus.CREATED)// 채팅방이 생성된 것을 나타내는 응답 코드
                    .body(new StartPrivateChannelResponseDto(dto.getChannelId(),
                            dto.getChatId(), "일대일 채팅이 시작되었습니다."));
        }
    }

    /**
     * 그룹 채널을 개설합니다.
     * 그룹 채널은 Storage 에 종속된 것으로, 특정 Storage
     * 를 조회하면 접근이 가능합니다.
     * 또한, 해당 Storage 의 위치 정보를 갖기 때문에 홈 화면에서 근처의 그룹챗을 띄우는 등의 접근 루트를 고려할 수 있습니다.
     * @param requestDto 창고 id (storageId), 기본 채널 제목(defaultTitle), 방 정원(limit)
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId)
     */
    @PostMapping("/group")
    public ResponseEntity<StartGroupChannelResponseDto> startGroupChannel(@RequestBody StartGroupChannelRequestDto requestDto,
                                                                          Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        // 이미 채팅이 존재 하는 경우
        channelRepository.findGroupChannelByStorageId(requestDto.getStorageId()).ifPresent(channel -> {
            throw new CustomException(ErrorCode.GROUP_CHANNEL_ALREADY_EXIST);
        });

        var createResult = chatService.createGroupChannel(userId, requestDto.getStorageId(), requestDto.getDefaultTitle(), requestDto.getLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(new StartGroupChannelResponseDto(createResult.getChannelId(), createResult.getChatId(), "그룹 채널이 개설되었습니다."));
    }

    /**
     * 근처에 있는 GroupChannel 을 조회합니다.
     * @param requestDto 경도(lon) 위도(lat), 반경(rad)
     * @return 근처 그룹 채널 정보
     */
    @GetMapping("/group/nearby")
    public ResponseEntity<FindGroupChannelNearbyResponseDto> findGroupChannelNearby(@Valid @ModelAttribute FindGroupChannelNearbyRequestDto requestDto) {
        Coord centerCoord = new Coord(requestDto.getLon(), requestDto.getLat());
        Point centerPoint = geomUtil.createPoint(centerCoord);
        List<GroupChannelDto> groupChatInfos = channelRepository.findGroupChannelNearBy(centerPoint, requestDto.getRad())
                .stream().map(GroupChannelDto::new).collect(Collectors.toList());
        if (groupChatInfos.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new FindGroupChannelNearbyResponseDto("근처에 진행중인 그룹 채팅이 없습니다.", centerCoord, requestDto.getRad(), groupChatInfos));
        }
        return ResponseEntity.ok().body(new FindGroupChannelNearbyResponseDto("가까운 그룹 채널이 조회 되었습니다", centerCoord, requestDto.getRad(), groupChatInfos));
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
        List<ParticipantInfoDto> chatParticipants = channelService.findParticipantsInChannel(channelId, userId);
        return ResponseEntity.ok().body(new FindChannelParticipantsResponseDto(chatParticipants.size(), chatParticipants));
    }
}
