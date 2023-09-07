package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.request.ChangeChatTitleRequestDto;
import com.example.naejango.domain.chat.dto.request.DeleteChatResponseDto;
import com.example.naejango.domain.chat.dto.response.ChangeChatTitleResponseDto;
import com.example.naejango.domain.chat.dto.response.FindChatResponseDto;
import com.example.naejango.domain.chat.dto.response.JoinGroupChatResponseDto;
import com.example.naejango.domain.chat.dto.response.MyChatListResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final ChannelService channelService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 그룹 채널에 입장 합니다. 해당 그룹 채널에 연결되는 Chat 객체를 생성합니다.
     * 이후 그룹 채널에서 메세지가 발송되면 해당 Chat 과 연관된 ChatMessage 이 생성되어 메세지를 받을 수 있습니다.
     * 채널의 최대 정원 도달시 입장이 불가합니다.
     * @param channelId 그룹 채널 id
     * @return 채널 id(channelId), 채팅방 id(chatId), 생성 결과(message)
     */
    @PostMapping("/group/{channelId}")
    public ResponseEntity<JoinGroupChatResponseDto> joinGroupChat(@PathVariable("channelId") Long channelId,
                                                                  Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        Optional<Long> groupChatOpt = chatRepository.findGroupChat(channelId, userId);

        if (groupChatOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new JoinGroupChatResponseDto(channelId, groupChatOpt.get(), "이미 참여중인 채널입니다."));
        }

        // 정원 초과 로직
        if(channelService.isFull(channelId)) throw new CustomException(ErrorCode.CHANNEL_IS_FULL);

        Long chatId = chatService.joinGroupChat(channelId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new JoinGroupChatResponseDto(channelId, chatId, "그룹 채팅이 시작되었습니다."));

    }

    /**
     * 요청 회원이 참여한 채널의 채팅방 id 를 찾습니다.
     * 채널에 참여 중이면 Chat 을 반환 합니다.
     * @param channelId 채널 id
     * @return 채널 참여 여부(hasChat), 챗 id(chatId)
     */
    @GetMapping("/{channelId}/myChat")
    public ResponseEntity<FindChatResponseDto> findChatByChannelId(@PathVariable("channelId") Long channelId,
                                                                   Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        Optional<Chat> chatOpt = chatRepository.findChatByChannelIdAndOwnerId(channelId, userId);
        if(chatOpt.isEmpty()) return ResponseEntity.ok().body(new FindChatResponseDto(null, "채널에 참여하고 있지 않습니다."));
        return ResponseEntity.ok().body(new FindChatResponseDto(chatOpt.get().getId(), "해당 채널의 채팅방을 조회했습니다."));
    }

    /**
     * 내가 참여하고 있는 chatroom 목록을 가장 최근 대화한 순으로 조회합니다.
     * @param page 요청 페이지
     * @param size 요청 결과 개수
     * @return 요청 유저 id(ownerId),
     *         채팅방 Info 리스트 (List<ChatInfoDto>) :
     *          채팅방 id(chatId), 채널 id(channelId), 채팅 타입(type),
     *          채팅방 제목(title), 마지막 대화(lastMessage), 안읽은 메세지(unreadMessages),
     *          마지막 대화나눈 시간(lastChatAt)
     */
    @GetMapping("")
    public ResponseEntity<MyChatListResponseDto> myChatList(@RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "10") int size,
                                                            Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(userId, PageRequest.of(page, size));
        return ResponseEntity.ok().body(new MyChatListResponseDto(page, size, result.hasNext(), result.getContent()));
    }

    /**
     * 채팅방의 제목을 변경합니다. *채널 제목 아님
     * @param requestDto 새로운 채팅방 제목(title)
     * @param chatId 변경하고자 하는 채팅방 id
     * @return 변경된 채팅방 id, 새로운 채팅방 제목(changedTitle)
     */
    @PatchMapping("/{chatId}")
    public ResponseEntity<ChangeChatTitleResponseDto> changeChatTitle(@RequestBody ChangeChatTitleRequestDto requestDto,
                                                                      @PathVariable("chatId") Long chatId,
                                                                      Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        chatService.changeChatTitle(userId, chatId, requestDto.getTitle());
        var responseDto = ChangeChatTitleResponseDto.builder().chatId(chatId).changedTitle(requestDto.getTitle()).build();
        return ResponseEntity.ok().body(responseDto);
    }

    /**
     * 채팅방을 종료합니다.
     * 일대일 채팅의 경우, Chat 과 연관된 ChatMessage 가 삭제 됩니다.
     * 그룹 채팅의 경우, Chat 과 연관된 ChatMessage 모두 삭제되며, channel 의 participantsCount 가 감소합니다.
     * Channel 과 연관된 ChatMessage 가 없으면 채널에 아무도 남지 않았다고 판단합니다.
     * 채널에 아무도 남지 않으면 Channel, Chat, ChatMessage, Message 가 전부 삭제됩니다.
     * @param chatId 나가고자 하는 채팅방 id(chatId)
     * @return 삭제된 채팅방 id(chatId), 삭제 메세지(message)
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<DeleteChatResponseDto> deleteChat(@PathVariable("chatId") Long chatId,
                                                            Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        chatService.deleteChat(userId, chatId);
        var responseDto = new DeleteChatResponseDto(chatId, "해당 채팅방을 종료했습니다.");
        return ResponseEntity.ok().body(responseDto);
    }
}
