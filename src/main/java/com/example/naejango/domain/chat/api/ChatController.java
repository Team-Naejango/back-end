package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.response.MyChatListResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.global.common.handler.AuthenticationHandler;
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
    private final AuthenticationHandler authenticationHandler;


    /**
     * 특정 회원과의 Private Chat 을 시작합니다.
     * 만약 이미 채팅방이 존재한다면 채팅방의 채널id 값을 반환하고
     * 존재하지 않으면 생성한 뒤, 채팅방의 채널id 값을 반환합니다.
     */
    @GetMapping("/private/{otherUserId}")
    public ResponseEntity<StartPrivateChatResponseDto> startPrivateChat(@PathVariable("otherUserId") Long otherUserId,
                                                                        Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        Optional<StartPrivateChatResponseDto> result = chatRepository.findPrivateChannelBetweenUsers(userId, otherUserId);
        if (result.isPresent()) {
            return ResponseEntity.ok().body(result.get());
        } else {
            StartPrivateChatResponseDto created = chatService.createPrivateChat(userId, otherUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created); // 채팅방이 생성된 것을 나타내는 응답 코드.
        }
    }

    /**
     * 내가 참여하고 있는 chatroom 목록을 조회합니다.
     * 가장 최근 대화한 순으로 페이징 처리를 하였습니다.
     * 요청 회원 id, nickname 및 채팅방 정보(chatroom id, title, type, recentMessage, lastChatAt) 를 반환합니다.
     * (private chatroom 의 title 은 조회한 회원에 따라 다르게 노출이 되어야 하므로, nickname 을 포함하여 반환합니다.)
     */
    @GetMapping("")
    public ResponseEntity<MyChatListResponseDto> myChatroomList(@RequestParam("page") int page,
                                                                @RequestParam("size") int size,
                                                                Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        Page<ChatInfoDto> result = chatRepository.findChatByOwnerIdOrderByLastChat(userId, PageRequest.of(page, size));
        return ResponseEntity.ok().body(new MyChatListResponseDto(userId, result.getContent()));
    }

}
