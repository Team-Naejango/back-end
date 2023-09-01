package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChatRequestDto;
import com.example.naejango.domain.chat.dto.response.FindChannelParticipantsResponseDto;
import com.example.naejango.domain.chat.dto.response.StartGroupChatResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChannelResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel")
public class ChannelController {

    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final ChannelService channelService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 특정 회원과의 Private Chat 을 시작합니다.
     * 만약 이미 채팅방이 존재한다면 채팅방의 채널 id 값을 반환하고
     * 존재하지 않으면 생성한 뒤, 채팅방의 채널id 값을 반환합니다.
     */
    @PostMapping ("/private/{otherUserId}")
    public ResponseEntity<StartPrivateChannelResponseDto> startPrivateChannel(@PathVariable("otherUserId") Long otherUserId,
                                                                              Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
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
     * 그룹채팅방을 개설합니다.
     * @param requestDto 기본 채널 제목(defaultTitle), 방 정원(limit)
     * @return 개설된 채팅 채널(channelId), 채팅방(chatId)
     */
    @PostMapping("/group")
    public ResponseEntity<StartGroupChatResponseDto> startGroupChannel(@RequestBody StartGroupChatRequestDto requestDto,
                                                                       Authentication authentication) {
        // 그룹채팅의 개수를 제한하거나 자격을 등 관련 로직이 필요할 것 같음
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        var createResult = chatService.createGroupChannel(userId, requestDto.getDefaultTitle(), requestDto.getLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(new StartGroupChatResponseDto(createResult.getChannelId(), createResult.getChatId()));
    }

    /**
     * 채팅 채널(channel)에 참여중인 유저의 정보를 조회합니다.
     * @param channelId 조회하고자 하는 채널 id(channelId)
     * @return 총 참여자(total)
     *         참여자 정보(List<ParticipantInfoDto>) :
     *         참여자 id(participantId), 닉네임(nickname), 이미지링크(imgUrl)
     */
    @GetMapping("/{channelId}/participants")
    public ResponseEntity<FindChannelParticipantsResponseDto> findChannelParticipants(@PathVariable("channelId") Long channelId,
                                                                                      Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        List<ParticipantInfoDto> chatParticipants = channelService.findChatParticipants(channelId, userId);
        return ResponseEntity.ok().body(new FindChannelParticipantsResponseDto(chatParticipants.size(), chatParticipants));
    }
}
