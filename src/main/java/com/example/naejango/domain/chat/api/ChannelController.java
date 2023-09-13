package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.CreateChannelDto;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.request.FindGroupChannelNearbyRequestDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChannelRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel")
public class ChannelController {
    private final ChannelService channelService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 특정 회원과의 Private Channel 을 개설합니다.
     * 만약 이미 채팅방이 존재한다면 해당 채팅방의 채널 id 및 요청자의 chatId 값을 반환하고
     * 존재하지 않으면 채널을 생성한 뒤 해당 값을 반환 합니다.
     * @param otherUserId 상대방 id
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId), 생성 결과(message)
     */
    @PostMapping ("/private/{otherUserId}")
    public ResponseEntity<CommonResponseDto<ChannelAndChatDto>> startPrivateChannel(@PathVariable("otherUserId") Long otherUserId,
                                                                 Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 채팅방 생성
        CreateChannelDto serviceDto = channelService.createPrivateChannel(userId, otherUserId);

        // 결과 생성
        ChannelAndChatDto result = new ChannelAndChatDto(serviceDto.getChannelId(), serviceDto.getChatId());

        // 반환
        if (serviceDto.isCreated()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CommonResponseDto<>("일대일 채널이 개설 되었습니다.", result)
            );
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new CommonResponseDto<>("이미 진행중인 채널이 있습니다.", result)
            );
        }
    }

    /**
     * 그룹 채널을 개설합니다.
     * 그룹 채널은 ItemsStorage 에 종속된 것으로, 특정 Storage 에서 조회하여 해당 item 에 할당된 그룹 채널에 접근 가능합니다.
     * 해당 item 이 등록된 storage 의 위치 정보를 통해 주변의 그룹 채널 검색이 가능합니다.
     * @param requestDto 창고 id (storageId), 기본 채널 제목(defaultTitle), 방 정원(limit)
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId)
     */
    @PostMapping("/group")
    public ResponseEntity<CommonResponseDto<ChannelAndChatDto>> startGroupChannel(@RequestBody StartGroupChannelRequestDto requestDto,
                                                                                  Authentication authentication) {

        Long userId = authenticationHandler.getUserId(authentication);

        // 그룹 채널 생성
        CreateChannelDto serviceDto = channelService.createGroupChannel(userId, requestDto.getItemId(), requestDto.getDefaultTitle(), requestDto.getLimit());

        // 결과 생성
        ChannelAndChatDto result = new ChannelAndChatDto(serviceDto.getChannelId(), serviceDto.getChatId());

        // 반환
        if (serviceDto.isCreated()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonResponseDto<>("그룹 채널이 개설되었습니다.", result));

        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CommonResponseDto<>("이미 진행중인 채널이 있습니다.", result));
        }
    }

    /**
     * 근처에 있는 GroupChannel 을 조회합니다.
     * @param requestDto 경도(lon) 위도(lat), 반경(rad)
     * @return 근처 그룹 채널 정보
     */
    @GetMapping("/group/nearby")
    public ResponseEntity<CommonResponseDto<List<GroupChannelDto>>> findGroupChannelNearby(@Valid @ModelAttribute FindGroupChannelNearbyRequestDto requestDto) {
        // 요청 정보 확인
        Coord center = new Coord(requestDto.getLon(), requestDto.getLat());
        int radius = requestDto.getRad();

        // 채널 목록 조회 - 기본적으로 10 개만 조회합니다.
        List<GroupChannelDto> result = channelService.findGroupChannelNearby(center, radius, 0, 10);

        // 반환
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonResponseDto<>("근처에 진행중인 그룹 채팅이 없습니다.", result));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CommonResponseDto<>("가까운 그룹 채널이 조회 되었습니다", result));
        }

    }

    /**
     * 채팅 채널(channel)에 참여중인 유저의 정보를 조회합니다.
     * @param channelId 조회하고자 하는 채널 id
     * @return 총 참여자(total)
     *         참여자 정보(List<ParticipantInfoDto>) :
     *         참여자 id(participantId), 닉네임(nickname), 이미지링크(imgUrl)
     */
    @GetMapping("/{channelId}/participants")
    public ResponseEntity<CommonResponseDto<List<ParticipantInfoDto>>> findChannelParticipants(@PathVariable("channelId") Long channelId,
                                                                                      Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        List<ParticipantInfoDto> result = channelService.findParticipantsInChannel(channelId, userId);

        // 반환
        return ResponseEntity.ok().body(new CommonResponseDto<>("채널 참여자 정보를 조회하였습니다.", result));
    }

    /**
     * 채팅방을 종료합니다.
     * 일대일 채팅의 경우, Chat 과 연관된 ChatMessage 가 삭제 됩니다.
     * 그룹 채팅의 경우, Chat 과 연관된 ChatMessage 모두 삭제되며, channel 의 participantsCount 가 감소합니다.
     * Channel 과 연관된 ChatMessage 가 없으면 채널에 아무도 남지 않았다고 판단합니다.
     * 채널에 아무도 남지 않으면 Channel, Chat, ChatMessage, Message 가 전부 삭제됩니다.
     * @param channelId 나가고자 하는 채널 id(channelId)
     * @return 삭제된 채팅방 id(chatId), 삭제 메세지(message)
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<CommonResponseDto<Long>> deleteChat(@PathVariable("channelId") Long channelId,
                                                              Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // Chat 삭제
        channelService.deleteChat(channelId, userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("해당 채널에서 퇴장하였습니다.", channelId));
    }
}
