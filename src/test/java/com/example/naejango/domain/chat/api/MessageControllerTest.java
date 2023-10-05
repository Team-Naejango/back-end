package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Collectors;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest extends RestDocsSupportTest {
    @MockBean
    MessageService messageServiceMock;
    @MockBean
    AuthenticationHandler authenticationHandlerMock;

    @Nested
    @DisplayName("최근 메세지 조회")
    class getRecentMessages {
        Channel channel = Channel.builder()
                .id(5L)
                .build();

        Message message1 = Message.builder()
                .id(3L)
                .messageType(MessageType.OPEN)
                .senderId(4L)
                .content(MessageType.OPEN.getDefaultMessage())
                .channel(channel)
                .build();

        Message message2 = Message.builder()
                .id(6L)
                .messageType(MessageType.CHAT)
                .senderId(6L)
                .content("안녕하세요")
                .channel(channel)
                .build();

        Message message3 = Message.builder()
                .id(7L)
                .messageType(MessageType.ENTER)
                .senderId(9L)
                .content(MessageType.ENTER.getDefaultMessage())
                .channel(channel)
                .build();

        Message message4 = Message.builder()
                .id(10L)
                .messageType(MessageType.EXIT)
                .senderId(11L)
                .content(MessageType.EXIT.getDefaultMessage())
                .channel(channel)
                .build();

        Message message5 = Message.builder()
                .id(12L)
                .messageType(MessageType.CLOSE)
                .senderId(13L)
                .content(MessageType.CLOSE.getDefaultMessage())
                .channel(channel)
                .build();

        List<Message> messageList = List.of(message5, message4, message3, message2, message1);

        @Test
        @DisplayName("조회 성공")
        @Tag("api")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any()))
                    .willReturn(1L);

            BDDMockito.given(messageServiceMock.recentMessages(1L, 2L, 0, 20))
                    .willReturn(messageList.stream().map(MessageDto::new).collect(Collectors.toList()));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/message/{chatId}", 2L)
                    .queryParam("page", "0")
                    .queryParam("size", "20")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(status().isOk());

            // restDocs
            resultActions.andDo(restDocs.document(resource(ResourceSnippetParameters.builder()
                            .tag("채팅")
                            .summary("채팅방 최근 메세지 조회")
                            .description("채팅방의 최근 메세지를 조회 합니다. 해당 채팅방의 메세지는 모두 읽음 처리 됩니다.\n\n" +
                                    "한번에 최대 300개의 메세지를 로드합니다.")
                            .requestParameters(
                                    parameterWithName("page").description("페이지"),
                                    parameterWithName("size").description("사이즈"),
                                    parameterWithName("_csrf").ignored())
                            .responseFields(
                                    fieldWithPath("message").description("결과 메세지"),
                                    fieldWithPath("result[].messageId").description("메세지 id"),
                                    fieldWithPath("result[].channelId").description("채널 id"),
                                    fieldWithPath("result[].senderId").description("보낸사람 id"),
                                    fieldWithPath("result[].messageType").description("메세지 타입"),
                                    fieldWithPath("result[].content").description("메세지 내용"),
                                    fieldWithPath("result[].sentAt").description("보낸 시각")
                            )
                            .build()
                    )));
        }

    }

}