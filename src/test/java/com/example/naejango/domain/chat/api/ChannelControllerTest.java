package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.ChatService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.CreateGroupChatDto;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.PrivateChatDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChannelRequestDto;
import com.example.naejango.domain.chat.dto.response.FindChannelParticipantsResponseDto;
import com.example.naejango.domain.chat.dto.response.FindGroupChannelNearbyResponseDto;
import com.example.naejango.domain.chat.dto.response.StartGroupChannelResponseDto;
import com.example.naejango.domain.chat.dto.response.StartPrivateChannelResponseDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.ErrorResponse;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
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
import java.util.stream.Collectors;

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
    private ChannelRepository channelRepository;
    @MockBean
    private GeomUtil geomUtilMock;
    @MockBean
    private AuthenticationHandler authenticationHandlerMock;
    private final GeomUtil geomUtil = new GeomUtil();
    @Nested
    class startPrivateChannel {
        User sender = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();
        User receiver = User.builder().id(2L).role(Role.USER).userKey("test_2").password("").build();

        Channel channel = Channel.builder().id(3L).build();
        Channel newChannel = Channel.builder().id(4L).build();

        Chat chat1 = Chat.builder().id(4L).chatType(ChatType.PRIVATE).ownerId(sender.getId()).build();

        @Test
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
        Storage storage = Storage.builder()
                .id(1L)
                .name("창고")
                .location(geomUtil.createPoint(127.02, 37.49))
                .build();
        StartGroupChannelRequestDto requestDto = StartGroupChannelRequestDto.builder()
                .storageId(storage.getId())
                .defaultTitle("공동구매")
                .limit(5)
                .build();

        User channelOwner = User.builder()
                .id(2L)
                .userKey("test")
                .build();


        GroupChannel groupChannel = GroupChannel.builder()
                .id(3L)
                .chatType(ChatType.GROUP)
                .ownerId(channelOwner.getId())
                .storageId(storage.getId())
                .build();
        Chat chat = Chat.builder()
                .id(3L)
                .title(requestDto.getDefaultTitle())
                .ownerId(channelOwner.getId())
                .channelId(groupChannel.getId())
                .chatType(ChatType.GROUP)
                .build();

        @Test
        @DisplayName("그룹 채널 개설 실패 : 이미 채널이 존재하는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(channelOwner.getId());
            BDDMockito.given(channelRepository.findGroupChannelByStorageId(requestDto.getStorageId())).willReturn(Optional.of(groupChannel));

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
            verify(chatServiceMock, times(0)).createGroupChannel(any(), any(), any(), anyInt());
            resultActions.andExpect(status().isConflict());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(ErrorResponse.toResponseEntity(ErrorCode.GROUP_CHANNEL_ALREADY_EXIST).getBody())));

        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 개설 성공")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.userIdFromAuthentication(any())).willReturn(channelOwner.getId());
            BDDMockito.given(channelRepository.findGroupChannelByStorageId(requestDto.getStorageId())).willReturn(Optional.empty());
            BDDMockito.given(chatServiceMock.createGroupChannel(channelOwner.getId(), storage.getId(), requestDto.getDefaultTitle(), requestDto.getLimit()))
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
            verify(chatServiceMock, times(1)).createGroupChannel(channelOwner.getId(), storage.getId(), requestDto.getDefaultTitle(), requestDto.getLimit());

            resultActions.andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new StartGroupChannelResponseDto(groupChannel.getId(), chat.getId(), "그룹 채널이 개설되었습니다.")
                    )));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("그룹 채팅방을 개설합니다.")
                                    .description("개인 채팅과는 달리 채팅방의 기본 제목, 정원, 창고 id 값을 입력 받습니다.")
                                    .requestFields(
                                            fieldWithPath("storageId").description("그룹 채팅이 할당될 창고 id"),
                                            fieldWithPath("defaultTitle").description("채팅 채널 디폴트 제목"),
                                            fieldWithPath("limit").description("채팅 채널 정원")
                                    )
                                    .responseFields(
                                            fieldWithPath("channelId").description("그룹 채널 id"),
                                            fieldWithPath("chatId").description("내 채팅방 id"),
                                            fieldWithPath("message").description("그룹 채널 개설 결과 메세지")
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
    class findGroupChannelNearby {
        User user = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();

        Storage storage1 = Storage.builder()
                .id(2L)
                .name("테스트 창고1")
                .location(geomUtil.createPoint(127.0371, 37.4951))
                .address("서울시 강남구")
                .build();

        Storage storage2 = Storage.builder()
                .id(3L)
                .name("테스트 창고1")
                .location(geomUtil.createPoint(127.0368, 37.4949))
                .address("서울시 강남구")
                .build();

        GroupChannel channel1 = GroupChannel.builder()
                .id(4L)
                .chatType(ChatType.GROUP)
                .storageId(storage1.getId())
                .participantsCount(3)
                .channelLimit(5)
                .ownerId(user.getId())
                .defaultTitle("그룹채널 1")
                .build();

        GroupChannel channel2 = GroupChannel.builder()
                .id(5L)
                .chatType(ChatType.GROUP)
                .storageId(storage2.getId())
                .participantsCount(2)
                .channelLimit(10)
                .ownerId(user.getId())
                .defaultTitle("그룹채널 2")
                .build();

        int radius = 2000;
        Coord coord = new Coord(127.037422, 37.4954475);
        Point point = geomUtil.createPoint(coord);
        @Test
        @DisplayName("조회 결과 없음")
        void test1() throws Exception {
            // given
            BDDMockito.given(geomUtilMock.createPoint(coord)).willReturn(point);
            BDDMockito.given(channelRepository.findGroupChannelNearBy(point, radius)).willReturn(Arrays.asList());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/channel/group/nearby")
                    .queryParam("lon", String.valueOf(coord.getLongitude()))
                    .queryParam("lat", String.valueOf(coord.getLatitude()))
                    .queryParam("rad", String.valueOf(radius))
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new FindGroupChannelNearbyResponseDto("근처에 진행중인 그룹 채팅이 없습니다.", coord, radius, Arrays.asList()))));
        }

        @Test
        @Tag("api")
        @DisplayName("조회 결과 있음")
        void test2() throws Exception {
            // given
            List<GroupChannel> groupChannels = Arrays.asList(channel1, channel2);
            List<GroupChannelDto> dtos = groupChannels.stream().map(GroupChannelDto::new).collect(Collectors.toList());
            BDDMockito.given(geomUtilMock.createPoint(coord)).willReturn(point);
            BDDMockito.given(channelRepository.findGroupChannelNearBy(point, radius)).willReturn(groupChannels);


            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/channel/group/nearby")
                    .queryParam("lon", String.valueOf(coord.getLongitude()))
                    .queryParam("lat", String.valueOf(coord.getLatitude()))
                    .queryParam("rad", String.valueOf(radius))
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new FindGroupChannelNearbyResponseDto("가까운 그룹 채널이 조회 되었습니다", coord, radius, dtos))));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("채팅")
                            .summary("근처의 그룹 채널 정보를 조회합니다.")
                            .requestParameters(
                                    parameterWithName("lon").description("조회하고자 하는 중심 경도"),
                                    parameterWithName("lat").description("조회하고자 하는 중심 위도"),
                                    parameterWithName("rad").description("조회 반경"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("center").description("조회한 중심 좌표"),
                                    fieldWithPath("center.longitude").description("조회한 중심 경도"),
                                    fieldWithPath("center.latitude").description("조회한 중심 위도"),
                                    fieldWithPath("radius").description("조회 반경"),
                                    fieldWithPath("groupChannelsNearby[]").description("조회한 그룹 채널 정보"),
                                    fieldWithPath("groupChannelsNearby[].channelId").description("채널 id"),
                                    fieldWithPath("groupChannelsNearby[].ownerId").description("채널 주인(창고 주인)"),
                                    fieldWithPath("groupChannelsNearby[].storageId").description("채널이 속한 창고 id"),
                                    fieldWithPath("groupChannelsNearby[].participantsCount").description("채널 참여자 수"),
                                    fieldWithPath("groupChannelsNearby[].defaultTitle").description("채널 제목"),
                                    fieldWithPath("groupChannelsNearby[].channelLimit").description("채널 정원")
                            ).responseSchema(
                                    Schema.schema("근처 그룹 채널 조회 Response")
                            ).build()
                    )));
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

        Channel groupChannel = GroupChannel.builder().id(4L).build();

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
            BDDMockito.given(channelServiceMock.findParticipantsInChannel(groupChannel.getId(), participant1.getId()))
                    .willReturn(infoListDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/channel/{channelId}/participants", groupChannel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).userIdFromAuthentication(any());
            verify(channelServiceMock, times(1)).findParticipantsInChannel(groupChannel.getId(), participant1.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content()
                    .json(objectMapper.writeValueAsString(new FindChannelParticipantsResponseDto(infoListDto.size(), infoListDto)))
            );

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("특정 채널에 참여한 참여자 정보를 조회합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("참여자 조회를 하고자 하는 채널 id")
                                    ).responseFields(
                                            fieldWithPath("total").description("채널에 속한 총원"),
                                            fieldWithPath("participants[]").description("채널 참여자 정보"),
                                            fieldWithPath("participants[].participantId").description("참여자 id"),
                                            fieldWithPath("participants[].nickname").description("참여자 닉네임"),
                                            fieldWithPath("participants[].imgUrl").description("참여자 사진 링크")
                                    ).responseSchema(
                                            Schema.schema("채널 참여자 조회 Response")
                                    ).build()
                    )
            ));
        }
    }


}