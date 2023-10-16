package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.ChatService;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.JoinGroupChannelDto;
import com.example.naejango.domain.chat.dto.request.ChangeChatTitleRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 그룹 채널 입장
     * 그룹 채널에 입장 합니다. 해당 그룹 채널에 연결되는 Chat 객체를 생성합니다.
     * 이후 그룹 채널에서 메세지가 발송되면 해당 Chat 과 연관된 ChatMessage 이 생성되어 메세지를 받을 수 있습니다.
     * 채널의 최대 정원 도달시 입장이 불가합니다.
     */
    @PostMapping("/group/{channelId}")
    public ResponseEntity<CommonResponseDto<ChannelAndChatDto>> joinGroupChannel(@PathVariable("channelId") Long channelId,
                                                                                 Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 그룹 채널 입장
        JoinGroupChannelDto serviceDto = chatService.joinGroupChannel(channelId, userId);

        // 결과 생성
        ChannelAndChatDto result = new ChannelAndChatDto(channelId, serviceDto.getChatId());

        // 결과 반환
        if (serviceDto.isCreated()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonResponseDto<>("그룹 채팅이 시작되었습니다.", result));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CommonResponseDto<>("이미 참여중인 채널입니다.", result));
        }
    }

    /**
     * 채팅방 목록 조회
     * 나의 chat 목록을 가장 최근 대화한 순으로 조회합니다.
     */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<ChatInfoDto>>> myChatList(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                                                           Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        List<ChatInfoDto> serviceDto = chatService.myChatList(userId, page, size);

        return ResponseEntity.ok().body(
                new CommonResponseDto<>("조회 성공", serviceDto)
        );
    }

    /**
     * 요청 회원이 참여한 채널의 ChatId 를 찾습니다.
     * 채널에 참여 중이면 Chat 을 반환 합니다.
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<CommonResponseDto<Long>> findChatByChannelId(@PathVariable("channelId") Long channelId,
                                                                   Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        Long chatId = chatService.myChatId(channelId, userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", chatId));
    }

    /**
     * Chat 의 제목을 변경합니다.
     */
    @PatchMapping("/{chatId}")
    public ResponseEntity<CommonResponseDto<String>> changeChatTitle(@RequestBody @Valid ChangeChatTitleRequestDto requestDto,
                                                                      @PathVariable("chatId") Long chatId,
                                                                      Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 요청 정보 조회
        String title = requestDto.getTitle();

        // 변경
        chatService.changeChatTitle(userId, chatId, title);

        return ResponseEntity.ok().body(new CommonResponseDto<>("변경 완료", title));
    }
    /**
     * Chat 을 삭제합니다.
     * 일대일 채팅의 경우, Chat 과 연관된 ChatMessage 가 삭제 됩니다.
     * 그룹 채팅의 경우, Chat 과 연관된 ChatMessage 모두 삭제되며, channel 의 participantsCount 가 감소합니다.
     * Channel 과 연관된 ChatMessage 가 없으면 채널에 아무도 남지 않았다고 판단합니다.
     * 채널에 아무도 남지 않으면 Channel, Chat, ChatMessage, Message 가 전부 삭제됩니다.
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<CommonResponseDto<Long>> deleteChat(@PathVariable("channelId") Long channelId,
                                                              Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // Chat 삭제
        chatService.deleteChatByChannelIdAndUserId(channelId, userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("해당 채널에서 퇴장하였습니다.", channelId));
    }

}
