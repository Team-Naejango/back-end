package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.request.ChangeChatTitleRequestDto;
import com.example.naejango.domain.chat.dto.request.DeleteChatResponseDto;
import com.example.naejango.domain.chat.dto.response.ChangeChatTitleResponseDto;
import com.example.naejango.domain.chat.dto.response.FindChatResponseDto;
import com.example.naejango.domain.chat.dto.response.JoinGroupChatResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.ErrorResponse;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends RestDocsSupportTest {
    @MockBean
    private ChatRepository chatRepositoryMock;
    @MockBean
    private ChatService chatServiceMock;
    @MockBean
    private ChannelService channelServiceMock;
    @MockBean
    private AuthenticationHandler authenticationHandlerMock;

    @Nested
    class joinGroupChat {
        User user = User.builder()
                .id(1L)
                .userKey("test")
                .build();

        Channel channel = GroupChannel.builder()
                .id(2L)
                .defaultTitle("공동구매")
                .chatType(ChatType.GROUP)
                .build();

        Chat chat = Chat.builder()
                .id(3L)
                .chatType(ChatType.GROUP)
                .channelId(channel.getId())
                .ownerId(user.getId())
                .build();

        @Test
        @DisplayName("그룹 채널 참여 : 이미 채널에 참여되어 있는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findGroupChat(channel.getId(), user.getId())).willReturn(Optional.of(chat.getId()));
            BDDMockito.given(channelServiceMock.isFull(channel.getId())).willReturn(false);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findGroupChat(channel.getId(), user.getId());

            resultActions.andExpect(status().isConflict());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new JoinGroupChatResponseDto(channel.getId(), chat.getId(), "이미 참여중인 채널입니다.")
            )));
        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 참여 : 정원 초과인 경우")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findGroupChat(channel.getId(), user.getId())).willReturn(Optional.empty());
            BDDMockito.given(channelServiceMock.isFull(channel.getId())).willReturn(true);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findGroupChat(channel.getId(), user.getId());

            resultActions.andExpect(status().isConflict());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(ErrorResponse.toResponseEntity(ErrorCode.CHANNEL_IS_FULL).getBody())));
        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 참여 : 참여 중이지 않은 경우")
        void test3() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findGroupChat(channel.getId(), user.getId())).willReturn(Optional.empty());
            BDDMockito.given(channelServiceMock.isFull(channel.getId())).willReturn(false);
            BDDMockito.given(chatServiceMock.joinGroupChat(channel.getId(), user.getId())).willReturn(chat.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findGroupChat(channel.getId(), user.getId());
            verify(chatServiceMock, times(1)).joinGroupChat(channel.getId(), user.getId());
            
            resultActions.andExpect(status().isCreated());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new JoinGroupChatResponseDto(channel.getId(), chat.getId(), "그룹 채팅이 시작되었습니다.")
            )));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("그룹 채널에 참여합니다.")
                                    .description("그룹 채널에 참여합니다. \n\n" +
                                            "이미 참여중인 채널인 경우 이미 채널에 참여중이라는 메세지와 함께 채팅방 id 를 응답합니다. \n\n" +
                                            "참여중이지 않은 채팅인 경우 채팅방의 정원을 확인하고 가득차 있지 않으면, 채팅방(Chat) 을 새로 생성하고 채널에 참여 합니다. \n\n" +
                                            "정원이 가득찬 경우 409 에러로 응답합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("그룹 채널 id")
                                    )
                                    .responseFields(
                                            fieldWithPath("channelId").description("참여한 채널 id"),
                                            fieldWithPath("chatId").description("내 채팅방 id"),
                                            fieldWithPath("message").description("그룹 채널 참여 결과 메세지")
                                    )
                                    .responseSchema(
                                            Schema.schema("그룹 채널 참여 Response")
                                    )
                                    .build()
                    )
            ));
        }
    }

    @Nested
    class findChatByChannelId {
        User user = User.builder()
                .id(1L)
                .userKey("test")
                .build();

        GroupChannel channel = GroupChannel.builder()
                .id(2L)
                .defaultTitle("공동구매")
                .chatType(ChatType.GROUP)
                .build();

        Chat chat = Chat.builder()
                .id(3L)
                .title(channel.getDefaultTitle())
                .ownerId(user.getId())
                .channelId(channel.getId())
                .chatType(ChatType.GROUP)
                .build();


        @Test
        @DisplayName("조회 결과 없음")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findChatByChannelIdAndOwnerId(channel.getId(), user.getId())).willReturn(Optional.empty());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat/{channelId}/myChat", channel.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findChatByChannelIdAndOwnerId(channel.getId(), user.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(new FindChatResponseDto(null, "채널에 참여하고 있지 않습니다."))));
        }
        @Test
        @Tag("api")
        @DisplayName("조회 결과 있음")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findChatByChannelIdAndOwnerId(channel.getId(), user.getId())).willReturn(Optional.of(chat));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat/{channelId}/myChat", channel.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findChatByChannelIdAndOwnerId(channel.getId(), user.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(new FindChatResponseDto(chat.getId(), "해당 채널의 채팅방을 조회했습니다."))));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("특정 채널의 내 채팅방 id 조회")
                                    .description("특정 채널에 속한 내 채팅방 id 를 조회합니다. \n\n" +
                                            "특정 채널에 유저가 참여하고 있는지 여부를 알 수 있으며, 참여 하고 있는 경우 해당 채팅방의 id 를 조회합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("조회하고자 하는 채널 id")
                                    ).responseFields(
                                            fieldWithPath("chatId").description("내 채팅방의 id(없으면 null)"),
                                            fieldWithPath("message").description("조회 결과 메세지")
                                    ).responseSchema(
                                            Schema.schema("특정 채널에 속한 내 채팅방 조회 Response")
                                    ).build())
            ));
        }
    }

    @Nested
    class myChatList {
        User user = User.builder().id(1L).role(Role.USER).userKey("test_me").password("").build();

        Chat chat1 = Chat.builder().id(2L).title("책상점").channelId(5L)
                .lastMessage("안녕하세요").ownerId(user.getId()).chatType(ChatType.PRIVATE).build();
        Chat chat2 = Chat.builder().id(3L).title("장터").channelId(6L)
                .lastMessage("물티슈팔아요").ownerId(user.getId()).chatType(ChatType.GROUP).build();
        Chat chat3 = Chat.builder().id(4L).title("공동구매방").channelId(7L)
                .lastMessage("싸네요").ownerId(user.getId()).chatType(ChatType.GROUP).build();

        ChatInfoDto chatInfo1 = new ChatInfoDto(chat1, 1);
        ChatInfoDto chatInfo2 = new ChatInfoDto(chat2, 2);
        ChatInfoDto chatInfo3 = new ChatInfoDto(chat3,3);
        @Test
        @Tag("api")
        @DisplayName("채팅방 조회 성공")
        void test1() throws Exception {
            // given
            List<ChatInfoDto> dtoList = List.of(chatInfo3, chatInfo2, chatInfo1);
            Page<ChatInfoDto> pagingResult = new PageImpl<>(dtoList, Pageable.unpaged(), dtoList.size());
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
            BDDMockito.given(chatRepositoryMock.findChatByOwnerIdOrderByLastChat(user.getId(), PageRequest.of(0, 10))).willReturn(pagingResult);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat")
                            .queryParam("page", "0")
                            .queryParam("size", "10")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );


            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findChatByOwnerIdOrderByLastChat(user.getId(), PageRequest.of(0, 10));

            resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("page").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("size").value(10))
                    .andExpect(MockMvcResultMatchers.jsonPath("hasNext").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("chatInfoList[0].chatId").value(4L))
                    .andExpect(MockMvcResultMatchers.jsonPath("chatInfoList[1].chatId").value(3L))
                    .andExpect( MockMvcResultMatchers.jsonPath("chatInfoList[2].chatId").value(2L));

            // restDocs
            resultActions.andDo(
                    restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("채팅")
                                            .summary("채팅방 목록을 가져옵니다.")
                                            .description("채팅방 목록을 시현하기 위해 채팅방 정보가 담긴 목록을 가지고 옵니다. \n\n")
                                            .responseFields(
                                                    fieldWithPath("page").description("요청한 페이지"),
                                                    fieldWithPath("size").description("요청한 결과물 수"),
                                                    fieldWithPath("hasNext").description("조회할 결과물이 남았는지 여부"),
                                                    fieldWithPath("chatInfoList[].chatId").description("채팅 Id"),
                                                    fieldWithPath("chatInfoList[].channelId").description("채널 Id"),
                                                    fieldWithPath("chatInfoList[].title").description("제목"),
                                                    fieldWithPath("chatInfoList[].chatType").description("채팅 타입(개인, 그룹)"),
                                                    fieldWithPath("chatInfoList[].lastMessage").description("마지막 대화 내용"),
                                                    fieldWithPath("chatInfoList[].unreadCount").description("안읽은 메세지 수"),
                                                    fieldWithPath("chatInfoList[].lastChatAt").description("마지막 대화 시각")
                                            )
                                            .responseSchema(
                                                    Schema.schema("내 채팅방 목록 Response")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

    @Nested
    class changeChatTitle {
        User user = User.builder()
                .id(1L)
                .build();

        Chat chat = Chat.builder()
                .id(2L)
                .title("변경 전 제목")
                .build();

        @Test
        @Tag("api")
        @DisplayName("변경 성공")
        void test1() throws Exception {
            // given
            var requestDto = ChangeChatTitleRequestDto.builder().title("변경한 제목").build();
            var responseDTo = ChangeChatTitleResponseDto.builder().chatId(chat.getId()).changedTitle("변경한 제목").build();
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .patch("/api/chat/{chatId}", chat.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatServiceMock, times(1)).changeChatTitle(user.getId(), chat.getId(), requestDto.getTitle());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(responseDTo)));

            // restDoc
            resultActions.andDo(restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("채팅")
                                            .summary("채팅방의 제목을 변경합니다.")
                                            .description("채팅방 제목을 변경합니다.\n\n " +
                                                    "채널의 기본 제목이 아니고 개개인에게 보여지는 채팅방의 제목입니다")
                                            .pathParameters(
                                                    parameterWithName("chatId").description("변경하고자 하는 채팅방 id")
                                            )
                                            .requestFields(
                                                    fieldWithPath("title").description("변경하고자 하는 제목")
                                            )
                                            .responseFields(
                                                    fieldWithPath("chatId").description("제목이 변경된 채팅방 id"),
                                                    fieldWithPath("changedTitle").description("변경된 제목")
                                            ).build()
                            )
                    )
            );
        }
    }

    @Nested
    class deleteChat {
        User user = User.builder()
                .id(1L)
                .build();

        Chat chat = Chat.builder()
                .id(2L)
                .title("변경 전 제목")
                .build();
        @Test
        @Tag("api")
        @DisplayName("대화방 나가기")
        void test1() throws Exception {
            // given
            DeleteChatResponseDto responseDto = DeleteChatResponseDto.builder().chatId(chat.getId()).message("해당 채팅방을 종료했습니다.").build();
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .delete("/api/chat/{chatId}", chat.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatServiceMock, times(1)).deleteChat(user.getId(), chat.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("채팅방을 종료합니다.")
                                    .description("채팅방을 종료합니다.\n\n" +
                                            "일대일 채팅의 경우, Chat 과 연관된 ChatMessage 가 삭제 됩니다.\n\n" +
                                            "그룹 채팅의 경우, Chat 과 연관된 ChatMessage 모두 삭제됩니다.\n\n" +
                                            "Channel 과 연관된 ChatMessage 가 없으면 채널에 아무도 남지 않았다고 판단합니다.\n\n" +
                                            "채널에 아무도 남지 않으면 Channel, Chat, ChatMessage, Message 가 전부 삭제됩니다.\n\n")
                                    .pathParameters(
                                            parameterWithName("chatId").description("종료하고자 하는 채팅방 id")
                                    ).responseFields(
                                            fieldWithPath("chatId").description("종료된 채팅방 id"),
                                            fieldWithPath("message").description("종료 결과를 알려주는 메세지")
                                    ).responseSchema(
                                            Schema.schema("채팅방 종료 Response")
                                    ).build()
                    ))
            );
        }

    }

}