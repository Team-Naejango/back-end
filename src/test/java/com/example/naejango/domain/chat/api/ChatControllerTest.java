package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.http.ChatService;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.JoinGroupChannelDto;
import com.example.naejango.domain.chat.dto.request.ChangeChatTitleRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends RestDocsSupportTest {
    @MockBean
    private ChatService chatServiceMock;
    @MockBean
    private AuthenticationHandler authenticationHandlerMock;

    @Nested
    @DisplayName("그룹 채널 참여")
    class joinGroupChannel {
        User user = User.builder()
                .id(1L)
                .userKey("test")
                .build();

        Channel channel = GroupChannel.builder()
                .id(2L)
                .defaultTitle("공동구매")
                .channelType(ChannelType.GROUP)
                .build();

        Chat chat = Chat.builder()
                .id(3L)
                .channel(channel)
                .owner(user)
                .build();

        @Test
        @DisplayName("그룹 채널 참여 : 이미 채널에 참여되어 있는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.joinGroupChannel(channel.getId(), user.getId()))
                    .willReturn(new JoinGroupChannelDto(false, chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).joinGroupChannel(channel.getId(), user.getId());

            resultActions.andExpect(status().isConflict());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("이미 참여중인 채널입니다.", new ChannelAndChatDto(channel.getId(), chat.getId()))
            )));
        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 참여 : 정원 초과인 경우")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.joinGroupChannel(channel.getId(), user.getId()))
                    .willThrow(new CustomException(ErrorCode.CHANNEL_IS_FULL));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).joinGroupChannel(channel.getId(), user.getId());
            Assertions.assertThrows(CustomException.class, () -> chatServiceMock.joinGroupChannel(channel.getId(), user.getId()));

            resultActions.andExpect(status().isConflict());
        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 참여 : 참여 중이지 않은 경우")
        void test3() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.joinGroupChannel(channel.getId(), user.getId()))
                    .willReturn(new JoinGroupChannelDto(true, chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/chat/group/{channelId}", channel.getId())
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).joinGroupChannel(channel.getId(), user.getId());
            
            resultActions.andExpect(status().isCreated());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("그룹 채팅이 시작되었습니다.", new ChannelAndChatDto(channel.getId(), chat.getId()))
            )));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("그룹 채널 참여")
                                    .description("그룹 채널에 참여합니다. \n\n" +
                                            "참여중이지 않은 채팅인 경우 채팅방(Chat) 을 새로 생성하고(채널 참여) 채팅방 id 를 반환합니다. \n\n" +
                                            "이미 참여중인 채널인 경우 이미 채널에 참여중이라는 메세지와 함께 채팅방 id 를 응답합니다. \n\n" +
                                            "정원이 가득찬 경우 409 에러 예외로 응답합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("그룹 채널 id")
                                    )
                                    .responseFields(
                                            fieldWithPath("message").description("결과 메세지"),
                                            fieldWithPath("result").description("결과"),
                                            fieldWithPath("result.channelId").description("참여한 채널 id"),
                                            fieldWithPath("result.chatId").description("내 채팅방 id")
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
    @DisplayName("내 채팅방 목록 조회")
    class myChatList {
        User user = User.builder().id(1L).role(Role.USER).userKey("test_me").password("").build();
        Item item = Item.builder().id(324L).itemType(ItemType.GROUP_BUY).build();

        Channel channel1 = GroupChannel.builder()
                .id(2L)
                .defaultTitle("공동구매")
                .channelLimit(5)
                .participantsCount(3)
                .item(item)
                .channelType(ChannelType.GROUP)
                .build();

        Channel channel2 = PrivateChannel.builder()
                .id(3L)
                .channelType(ChannelType.PRIVATE)
                .build();

        Channel channel3 = PrivateChannel.builder()
                .id(4L)
                .channelType(ChannelType.PRIVATE)
                .build();

        Chat chat1 = Chat.builder().id(3L).title("자바의정석").channel(channel2).owner(user).build();
        Chat chat2 = Chat.builder().id(4L).title("책공구").channel(channel1).owner(user).build();
        Chat chat3 = Chat.builder().id(5L).title("스프링").channel(channel3).owner(user).build();

        ChatInfoDto chatInfo1 = new ChatInfoDto(chat1, channel2, 1);
        ChatInfoDto chatInfo2 = new ChatInfoDto(chat2, channel1, 2);
        ChatInfoDto chatInfo3 = new ChatInfoDto(chat3, channel3, 3);

        @Test
        @Tag("api")
        @DisplayName("조회 성공")
        void test1() throws Exception {
            // given
            List<ChatInfoDto> dtoList = List.of(chatInfo3, chatInfo2, chatInfo1);
            Page<ChatInfoDto> pagingResult = new PageImpl<>(dtoList, Pageable.unpaged(), dtoList.size());
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.myChatList(user.getId(), 0, 10)).willReturn(pagingResult);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat")
                            .queryParam("page", "0")
                            .queryParam("size", "10")
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).myChatList(user.getId(), 0, 10);

            resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value("조회 성공"));

            // restDocs
            resultActions.andDo(
                    restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("채팅")
                                            .summary("내 채팅방 목록 조회")
                                            .description("내가 참여하고 있는 채널의 내 채팅방의 정보가 담긴 목록을 가지고 옵니다. \n\n")
                                            .requestParameters(
                                                    parameterWithName("page").description("조회 페이지").defaultValue("0").optional(),
                                                    parameterWithName("size").description("조회 결과물 수").defaultValue("10").optional()
                                            )
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result[]").description("채팅방 리스트"),
                                                    fieldWithPath("result[].chatId").description("채팅 Id"),
                                                    fieldWithPath("result[].channelId").description("채널 Id"),
                                                    fieldWithPath("result[].itemId").description("아이템 Id"),
                                                    fieldWithPath("result[].title").description("제목"),
                                                    fieldWithPath("result[].channelType").description("채널 타입(개인, 그룹)"),
                                                    fieldWithPath("result[].participantsCount").description("현재 참여 인원"),
                                                    fieldWithPath("result[].channelLimit").description("채널 정원"),
                                                    fieldWithPath("result[].lastMessage").description("마지막 대화 내용"),
                                                    fieldWithPath("result[].unreadCount").description("안읽은 메세지 수"),
                                                    fieldWithPath("result[].lastChatAt").description("마지막 대화 시각")
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
    @DisplayName("채널의 내 채팅방 ID 조회")
    class findChatByChannelId {
        User user = User.builder()
                .id(1L)
                .userKey("test")
                .build();

        GroupChannel channel1 = GroupChannel.builder()
                .id(2L)
                .defaultTitle("공동구매")
                .channelType(ChannelType.GROUP)
                .build();

        Chat chat1 = Chat.builder()
                .id(5L)
                .title(channel1.getDefaultTitle())
                .channel(channel1)
                .owner(user)
                .build();

        @Test
        @DisplayName("조회 결과 없음")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.myChatId(channel1.getId(), user.getId()))
                    .willThrow(new CustomException(ErrorCode.CHAT_NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat/{channelId}", channel1.getId())
                            .header("Authorization", "Bearer {accessToken}")
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).myChatId(channel1.getId(), user.getId());

            resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @Tag("api")
        @DisplayName("조회 결과 있음")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            BDDMockito.given(chatServiceMock.myChatId(channel1.getId(), user.getId()))
                    .willReturn(chat1.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/chat/{channelId}", channel1.getId())
                            .header("Authorization", "Bearer {accessToken}")
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).myChatId(channel1.getId(), user.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("조회 성공", chat1.getId())))
            );

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("내 채팅방 id 조회")
                                    .description("특정 채널에 속한 내 채팅방 id 를 조회합니다. \n\n" +
                                            "특정 채널에 유저가 참여하고 있는지 여부를 알 수 있으며, 참여 하고 있는 경우 해당 채팅방의 id 를 조회합니다. \n\n" +
                                            "없는 경우 404 Not_Found 를 반환 합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("조회하고자 하는 채널 id")
                                    ).responseFields(
                                            fieldWithPath("message").description("조회 결과 메세지"),
                                            fieldWithPath("result").description("내 채팅방 ID")
                                    ).responseSchema(
                                            Schema.schema("특정 채널에 속한 내 채팅방 조회 Response")
                                    ).build())
            ));
        }
    }

    @Nested
    @DisplayName("채팅방 제목 수정")
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
            var responseDto = new CommonResponseDto<>("변경 완료", "변경한 제목");
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());
            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .patch("/api/chat/{chatId}", chat.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestDto))
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).changeChatTitle(user.getId(), chat.getId(), requestDto.getTitle());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

            // restDoc
            resultActions.andDo(restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("채팅")
                                            .summary("채팅방 제목 수정")
                                            .description("채팅방 제목을 변경합니다.\n\n " +
                                                    "채널의 기본 제목이 아니고 개개인에게 보여지는 채팅방의 제목입니다")
                                            .pathParameters(
                                                    parameterWithName("chatId").description("변경하고자 하는 채팅방 id")
                                            )
                                            .requestFields(
                                                    fieldWithPath("title").description("변경하고자 하는 제목")
                                            )
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result").description("변경된 제목")
                                            ).build()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("채널 퇴장, Chat 삭제")
    class deleteChat {
        User user = User.builder()
                .id(1L)
                .build();

        Channel channel = Channel.builder()
                .channelType(ChannelType.GROUP)
                .id(2L).build();

        @Test
        @Tag("api")
        @DisplayName("삭제 성공")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .delete("/api/chat/{channelId}", channel.getId())
            );

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(chatServiceMock, times(1)).deleteChatByChannelIdAndUserId(channel.getId(), user.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("해당 채널에서 퇴장하였습니다.", channel.getId())
            )));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("채팅방을 종료합니다.")
                                    .description("채팅방을 종료합니다.\n\n" +
                                            "일대일 채팅의 경우, Chat은 삭제되지 않고 연관된 ChatMessage 가 삭제됩니다.\n\n" +
                                            "그룹 채팅의 경우, Chat 과 연관된 ChatMessage 모두 삭제됩니다.\n\n" +
                                            "Channel 과 연관된 Chat 이 없으면 채널에 아무도 남지 않았다고 판단합니다.\n\n" +
                                            "채널에 아무도 남지 않으면 Channel, Chat, ChatMessage, Message 가 전부 삭제됩니다.\n\n")
                                    .pathParameters(
                                            parameterWithName("channelId").description("종료하고자 하는 채팅방 id")
                                    ).responseFields(
                                            fieldWithPath("result").description("종료된 채팅방 id"),
                                            fieldWithPath("message").description("종료 결과를 알려주는 메세지")
                                    ).responseSchema(
                                            Schema.schema("채팅방 종료 Response")
                                    ).build()
                    ))
            );
        }

    }
}