package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.dto.ChatInfoDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChatResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends RestDocsSupportTest {
    @MockBean
    private ChatRepository chatRepositoryMock;
    @MockBean
    private ChatService chatServiceMock;
    @MockBean
    private CommonDtoHandler commonDtoHandlerMock;

    @Test
    @Tag("api")
    @DisplayName("1대1 채팅하기 : 채팅 채널이 이미 존재하는 경우")
    void test1() throws Exception {
        // given
        User sender = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();
        User receiver = User.builder().id(2L).role(Role.USER).userKey("test_2").password("").build();

        Channel channel = Channel.builder().id(3L).build();
        Channel newChannel = Channel.builder().id(4L).build();

        Chat chat1 = Chat.builder().id(4L).type(ChatType.PRIVATE).ownerId(sender.getId()).build();
        Chat chat2 = Chat.builder().id(5L).type(ChatType.PRIVATE).ownerId(receiver.getId()).build();

        BDDMockito.given(commonDtoHandlerMock.userIdFromAuthentication(any())).willReturn(sender.getId());
        BDDMockito.given(chatRepositoryMock.findPrivateChannelBetweenUsers(sender.getId(), receiver.getId()))
                .willReturn(Optional.of(new StartPrivateChatResponseDto(channel.getId(), chat1.getId())));


        BDDMockito.given(chatServiceMock.createPrivateChat(sender.getId(), receiver.getId()))
                .willReturn(new StartPrivateChatResponseDto(newChannel.getId(), chat1.getId()));

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/chat/private/{receiverId}", receiver.getId())
                        .header("Authorization", "Bearer {accessToken}")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        verify(commonDtoHandlerMock, times(1)).userIdFromAuthentication(any());
        verify(chatRepositoryMock, times(1)).findPrivateChannelBetweenUsers(sender.getId(), receiver.getId());
        verify(chatServiceMock, never()).createPrivateChat(anyLong(), anyLong());

        resultActions.andExpect(status().isOk())
                .andExpect(
                        content().json(objectMapper.writeValueAsString(new StartPrivateChatResponseDto(channel.getId(), chat1.getId())))
                );
    }

    @Test
    @Tag("api")
    @DisplayName("1대1 채팅하기 : 채팅방이 존재하지 않는 경우")
    void test2() throws Exception {
        // given
        User sender = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();
        User receiver = User.builder().id(2L).role(Role.USER).userKey("test_2").password("").build();

        Channel channel = Channel.builder().id(3L).build();
        Channel newChannel = Channel.builder().id(4L).build();

        Chat chat1 = Chat.builder().id(4L).type(ChatType.PRIVATE).ownerId(sender.getId()).build();
        Chat chat2 = Chat.builder().id(5L).type(ChatType.PRIVATE).ownerId(receiver.getId()).build();

        BDDMockito.given(commonDtoHandlerMock.userIdFromAuthentication(any())).willReturn(sender.getId());
        BDDMockito.given(chatRepositoryMock.findPrivateChannelBetweenUsers(sender.getId(), receiver.getId()))
                .willReturn(Optional.empty());
        BDDMockito.given(chatServiceMock.createPrivateChat(sender.getId(), receiver.getId()))
                .willReturn(new StartPrivateChatResponseDto(newChannel.getId(), chat1.getId()));

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/chat/private/{receiverId}", receiver.getId())
                        .header("Authorization", "Bearer {accessToken}")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        verify(commonDtoHandlerMock, times(1)).userIdFromAuthentication(any());
        verify(chatRepositoryMock, times(1)).findPrivateChannelBetweenUsers(sender.getId(), receiver.getId());
        verify(chatServiceMock, times(1)).createPrivateChat(sender.getId(), receiver.getId());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(new StartPrivateChatResponseDto(newChannel.getId(), chat1.getId()))));

        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("특정 회원에 대하여 일대일 채팅의 채널 id 및 채팅 id 를 반환합니다.")
                                .pathParameters(
                                        parameterWithName("receiverId").description("상대방 id")
                                )
                                .responseFields(
                                        fieldWithPath("channelId").description("채팅 채널 id"),
                                        fieldWithPath("chatId").description("내 채팅방 id")
                                )
                                .build()
                )
        ));
    }

    @Test
    @Tag("api")
    @DisplayName("내 채팅방 조회")
    void test3() throws Exception {
        // given
        User user = User.builder().id(1L).role(Role.USER).userKey("test_me").password("").build();

        Chat chat1 = Chat.builder().id(2L).title("너만오면고").channelId(5L)
                .lastMessage("안녕하세요").ownerId(user.getId()).type(ChatType.PRIVATE).build();
        Chat chat2 = Chat.builder().id(3L).title("고수만").channelId(6L)
                .lastMessage("고고").ownerId(user.getId()).type(ChatType.PRIVATE).build();
        Chat chat3 = Chat.builder().id(4L).title("빨무3:3").channelId(7L)
                .lastMessage("ㅎㅇ").ownerId(user.getId()).type(ChatType.GROUP).build();

        ChatInfoDto chatInfo1 = new ChatInfoDto(chat1, 1);
        ChatInfoDto chatInfo2 = new ChatInfoDto(chat2, 2);
        ChatInfoDto chatInfo3 = new ChatInfoDto(chat3,3);


        // when
        BDDMockito.given(commonDtoHandlerMock.userIdFromAuthentication(any())).willReturn(user.getId());
        BDDMockito.given(chatRepositoryMock.findChatByOwnerIdOrderByLastChatTime(user.getId(), PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(List.of(chatInfo3, chatInfo2, chatInfo1)));

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/chat")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );


        // then
        verify(commonDtoHandlerMock, times(1)).userIdFromAuthentication(any());
        verify(chatRepositoryMock, times(1)).findChatByOwnerIdOrderByLastChatTime(user.getId(), PageRequest.of(0, 10));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("ownerId").value(1L))
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
                                        .description("채팅방의 간략한 정보가 담긴 목록을 가지고 옵니다.\n\n" +
                                                "채팅방 목록을 시현하기 위한 모든 정보가 담겨있습니다.\n\n")
                                        .responseFields(
                                                fieldWithPath("ownerId").description("채팅 주인"),
                                                fieldWithPath("chatInfoList[].chatId").description("채팅 Id"),
                                                fieldWithPath("chatInfoList[].channelId").description("채널 Id"),
                                                fieldWithPath("chatInfoList[].title").description("제목"),
                                                fieldWithPath("chatInfoList[].type").description("채팅 타입(개인, 그룹)"),
                                                fieldWithPath("chatInfoList[].lastMessage").description("마지막 대화 내용"),
                                                fieldWithPath("chatInfoList[].unreadMessages").description("안읽은 메세지 수"),
                                                fieldWithPath("chatInfoList[].lastChatAt").description("마지막 대화 시각")
                                        )
                                        .responseSchema(
                                                Schema.schema("내 채팅방 목록 응답")
                                        )
                                        .build()
                        )
                )
        );

    }

}