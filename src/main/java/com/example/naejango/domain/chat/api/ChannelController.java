package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.CreateChannelDto;
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
import java.util.Optional;

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
            return ResponseEntity.status(HttpStatus.OK).body(
                    new CommonResponseDto<>("이미 진행중인 채널이 있습니다.", result)
            );
        }
    }

    /**
     * 그룹 채널을 개설합니다.
     * 그룹 채널은 ItemsStorage 에 종속된 것으로, 특정 Storage 에서 조회하여 해당 item 에 할당된 그룹 채널에 접근 가능합니다.
     * 해당 item 이 등록된 storage 의 위치 정보를 통해 주변의 그룹 채널 검색이 가능합니다.
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

    /** 공동구매 아이템에 등록된 그룹 채팅 조회 */
    @GetMapping("/group/{itemId}")
    public ResponseEntity<CommonResponseDto<GroupChannelDto>> findGroupChannel(@PathVariable Long itemId) {

        Optional<GroupChannelDto> serviceDtoOptional = channelService.findGroupChannel(itemId);

        if (serviceDtoOptional.isEmpty()) {
            return ResponseEntity.ok().body(new CommonResponseDto<>("등록된 그룹 채널이 없습니다.", null));
        }
        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", serviceDtoOptional.get()));
    }

    /** 근처에 있는 GroupChannel 조회 */
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

    /** 채팅 채널(channel)에 참여중인 유저의 정보 조회 */
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
     * 채널 종료
     * 채널의 isClosed 필드를 이용하여 종료 처리 합니다.
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<CommonResponseDto<Void>> closeGroupChannel(@PathVariable Long channelId,
                                                                     Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        channelService.closeChannelById(channelId, userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("채널이 종료되었습니다.", null));
    }

}
