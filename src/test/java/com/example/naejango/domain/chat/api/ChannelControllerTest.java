package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.dto.CreateGroupChatDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChatRequestDto;
import com.example.naejango.domain.chat.dto.response.FindChannelParticipantsResponseDto;
import com.example.naejango.domain.chat.dto.response.StartGroupChatResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChannelResponseDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest extends RestDocsSupportTest {
    @MockBean
    private ChatRepository chatRepositoryMock;
    @MockBean
    private ChatService chatServiceMock;
    @MockBean
    private ChannelService channelServiceMock;
    @MockBean
    private AuthenticationHandler authenticationHandlerMock;
    @Nested
    class startPrivateChannel {
        User sender = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();
        User receiver = User.builder().id(2L).role(Role.USER).userKey("test_2").password("").build();

        Channel channel = Channel.builder().id(3L).build();
        Channel newChannel = Channel.builder().id(4L).build();

        Chat chat1 = Chat.builder().id(4L).type(ChatType.PRIVATE).ownerId(sender.getId()).build();
        Chat chat2 = Chat.builder().id(5L).type(ChatType.PRIVATE).ownerId(receiver.getId()).build();

        @Test
        @Tag("api")
        @DisplayName("일대일 채널 개설 - 채팅 채널이 이미 존재하는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(sender.getId());
            BDDMockito.given(chatRepositoryMock.findPrivateChannelBetweenUsers(sender.getId(), receiver.getId()))
                    .willReturn(Optional.of(new PrivateChatDto(channel.getId(), chat1.getId())));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/private/{receiverId}", receiver.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findPrivateChannelBetweenUsers(sender.getId(), receiver.getId());
            verify(chatServiceMock, never()).createPrivateChannel(anyLong(), anyLong());

            resultActions.andExpect(status().isConflict())
                    .andExpect(
                            content().json(objectMapper.writeValueAsString(
                                    new StartPrivateChannelResponseDto(channel.getId(), chat1.getId(), "이미 진행중인 채팅이 있습니다.")))
                    );
        }

        @Test
        @Tag("api")
        @DisplayName("일대일 채널 개설 - 채팅방이 존재하지 않는 경우")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(sender.getId());
            BDDMockito.given(chatRepositoryMock.findPrivateChannelBetweenUsers(sender.getId(), receiver.getId()))
                    .willReturn(Optional.empty());
            BDDMockito.given(chatServiceMock.createPrivateChannel(sender.getId(), receiver.getId()))
                    .willReturn(new PrivateChatDto(newChannel.getId(), chat1.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/private/{receiverId}", receiver.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatRepositoryMock, times(1)).findPrivateChannelBetweenUsers(sender.getId(), receiver.getId());
            verify(chatServiceMock, times(1)).createPrivateChannel(sender.getId(), receiver.getId());

            resultActions.andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new StartPrivateChannelResponseDto(newChannel.getId(), chat1.getId(), "일대일 채팅이 시작되었습니다.")
                    )));

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
                                            fieldWithPath("chatId").description("내 채팅방 id"),
                                            fieldWithPath("message").description("결과 메세지")
                                    )
                                    .build()
                    )
            ));
        }
    }

    @Nested
    class startGroupChannel {
        StartGroupChatRequestDto requestDto = StartGroupChatRequestDto.builder()
                .defaultTitle("공동구매")
                .limit(5)
                .build();

        User channelOwner = User.builder()
                .id(1L)
                .userKey("test")
                .build();

        Channel groupChannel = Channel.builder()
                .id(2L)
                .type(ChatType.GROUP)
                .defaultTitle(requestDto.getDefaultTitle())
                .ownerId(channelOwner.getId())
                .channelLimit(5)
                .build();

        Chat chat = Chat.builder()
                .id(3L)
                .title(groupChannel.getDefaultTitle())
                .ownerId(channelOwner.getId())
                .channelId(groupChannel.getId())
                .type(ChatType.GROUP)
                .build();

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 개설")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(channelOwner.getId());
            BDDMockito.given(chatServiceMock.createGroupChannel(channelOwner.getId(), requestDto.getDefaultTitle(), requestDto.getLimit()))
                    .willReturn(new CreateGroupChatDto(groupChannel.getId(), chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/group")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(chatServiceMock, times(1)).createGroupChannel(channelOwner.getId(), requestDto.getDefaultTitle(), requestDto.getLimit());

            resultActions.andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new StartGroupChatResponseDto(groupChannel.getId(), chat.getId())
                    )));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("그룹 채팅방을 개설합니다.")
                                    .description("개인 채팅과는 달리 채팅 채널의 디폴트 제목이 있어야 합니다. 또한 추후 추가 고려중인 정원 기능 구현을 위해 채팅방 정원도 설정하도록 해놓았습니다.")
                                    .requestFields(
                                            fieldWithPath("defaultTitle").description("채팅 채널 디폴트 제목"),
                                            fieldWithPath("limit").description("채팅 채널 정원")
                                    )
                                    .responseFields(
                                            fieldWithPath("channelId").description("그룹 채널 id"),
                                            fieldWithPath("chatId").description("내 채팅방 id")
                                    )
                                    .requestSchema(
                                            Schema.schema("그룹 채널 개설 Request")
                                    )
                                    .responseSchema(
                                            Schema.schema("그룹 채널 개설 Response")
                                    )
                                    .build()
                    )
            ));
        }
    }

    @Nested
    class findChatParticipants {
        UserProfile profile1 = UserProfile.builder().nickname("참가자1").imgUrl("사진1").build();
        UserProfile profile2 = UserProfile.builder().nickname("참가자2").imgUrl("사진2").build();
        UserProfile profile3 = UserProfile.builder().nickname("참가자3").imgUrl("사진3").build();

        User participant1 = User.builder().userProfile(profile1).id(1L).build();
        User participant2 = User.builder().userProfile(profile2).id(2L).build();
        User participant3 = User.builder().userProfile(profile3).id(3L).build();

        Channel groupChannel = Channel.builder().id(4L).defaultTitle("공동 구매").type(ChatType.GROUP).build();

        Chat chat1 = Chat.builder().type(ChatType.GROUP).ownerId(participant1.getId()).channelId(groupChannel.getId()).build();
        Chat chat2 = Chat.builder().type(ChatType.GROUP).ownerId(participant2.getId()).channelId(groupChannel.getId()).build();
        Chat chat3 = Chat.builder().type(ChatType.GROUP).ownerId(participant3.getId()).channelId(groupChannel.getId()).build();

        @Test
        @Tag("api")
        @DisplayName("조회 성공")
        void test1() throws Exception {
            // given
            List<ParticipantInfoDto> infoListDto = Arrays.asList(
                    new ParticipantInfoDto(participant1.getId(), participant1.getUserProfile().getNickname(), participant1.getUserProfile().getImgUrl()),
                    new ParticipantInfoDto(participant2.getId(), participant2.getUserProfile().getNickname(), participant2.getUserProfile().getImgUrl()),
                    new ParticipantInfoDto(participant3.getId(), participant3.getUserProfile().getNickname(), participant3.getUserProfile().getImgUrl()));
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(participant1.getId());
            BDDMockito.given(channelServiceMock.findChatParticipants(groupChannel.getId(), participant1.getId()))
                    .willReturn(infoListDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/channel/{channelId}/participants", groupChannel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(channelServiceMock, times(1)).findChatParticipants(groupChannel.getId(), participant1.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content()
                    .json(objectMapper.writeValueAsString(new FindChannelParticipantsResponseDto(infoListDto.size(), infoListDto)))
            );
        }
    }



}