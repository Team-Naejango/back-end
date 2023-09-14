package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.ChatService;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.JoinGroupChannelDto;
import com.example.naejango.domain.chat.dto.request.ChangeChatTitleRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
     * 그룹 채널에 입장 합니다. 해당 그룹 채널에 연결되는 Chat 객체를 생성합니다.
     * 이후 그룹 채널에서 메세지가 발송되면 해당 Chat 과 연관된 ChatMessage 이 생성되어 메세지를 받을 수 있습니다.
     * 채널의 최대 정원 도달시 입장이 불가합니다.
     * @param channelId 그룹 채널 id
     * @return 채널 id(channelId), 채팅방 id(chatId), 생성 결과(message)
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
     * 나의 chat 목록을 가장 최근 대화한 순으로 조회합니다.
     * @param page 요청 페이지
     * @param size 요청 결과 개수
     * @return 요청 유저 id(ownerId),
     *         채팅방 Info 리스트 (List<ChatInfoDto>) :
     *          채팅방 id(chatId), 채널 id(channelId), 채널 타입(channelType),
     *          채팅방 제목(title), 마지막 대화(lastMessage), 안읽은 메세지(unreadMessages),
     *          마지막 대화나눈 시간(lastChatAt)
     */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<ChatInfoDto>>> myChatList(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                                                           Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        Page<ChatInfoDto> serviceDto = chatService.myChatList(userId, page, size);

        return ResponseEntity.ok().body(
                new CommonResponseDto<>("조회 성공", serviceDto.getContent())
        );
    }

    /**
     * 요청 회원이 참여한 채널의 채팅방 id 를 찾습니다.
     * 채널에 참여 중이면 Chat 을 반환 합니다.
     * @param channelId 채널 id
     * @return 채널 참여 여부(hasChat), 챗 id(chatId)
     */
    @GetMapping("/{channelId}/myChat")
    public ResponseEntity<CommonResponseDto<Long>> findChatByChannelId(@PathVariable("channelId") Long channelId,
                                                                   Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        Long chatId = chatService.myChatId(channelId, userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", chatId));
    }

    /**
     * 채팅방의 제목을 변경합니다. *채널 제목 아님
     * @param requestDto 새로운 채팅방 제목(title)
     * @param chatId 변경하고자 하는 채팅방 id
     * @return 변경된 채팅방 id, 새로운 채팅방 제목(changedTitle)
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

}
