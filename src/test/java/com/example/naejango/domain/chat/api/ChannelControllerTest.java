package com.example.naejango.domain.chat.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.Chat;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.ChannelAndChatDto;
import com.example.naejango.domain.chat.dto.CreateChannelDto;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import com.example.naejango.domain.chat.dto.request.StartGroupChannelRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
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
import java.util.ArrayList;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest extends RestDocsSupportTest {
    @MockBean
    ChannelService channelServiceMock;
    @MockBean
    AuthenticationHandler authenticationHandlerMock;
    GeomUtil geomUtil = new GeomUtil();

    @Nested
    @DisplayName("일대일 채널 개설")
    class startPrivateChannel {
        User sender = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();
        User receiver = User.builder().id(2L).role(Role.USER).userKey("test_2").password("").build();

        Channel channel = Channel.builder().id(3L).build();

        Chat chat = Chat.builder().id(4L).owner(sender).build();

        @Test
        @DisplayName("실패 - 채팅 채널이 이미 존재하는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(sender.getId());
            BDDMockito.given(channelServiceMock.createPrivateChannel(sender.getId(), receiver.getId()))
                    .willReturn(new CreateChannelDto(false, channel.getId(), chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/private/{receiverId}", receiver.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(channelServiceMock, times(1)).createPrivateChannel(anyLong(), anyLong());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("이미 진행중인 채널이 있습니다.", new ChannelAndChatDto(channel.getId(), chat.getId()))
            )));
        }

        @Test
        @Tag("api")
        @DisplayName("성공 - 채팅방이 존재하지 않는 경우")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(sender.getId());
            BDDMockito.given(channelServiceMock.createPrivateChannel(sender.getId(), receiver.getId()))
                    .willReturn(new CreateChannelDto(true, channel.getId(), chat.getId()));


            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/private/{receiverId}", receiver.getId())
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(channelServiceMock, times(1)).createPrivateChannel(sender.getId(), receiver.getId());

            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("일대일 채널이 개설 되었습니다.", new ChannelAndChatDto(channel.getId(), chat.getId()))
            )));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("일대일 채널 개설")
                                    .description("특정 회원과의 일대일 채널을 개설하고 채널 및 채팅방 ID 를 반환합니다. \n\n" +
                                                "이미 진행 중인 경우에는 채널 및 채팅방 ID 를 반환합니다.")
                                    .pathParameters(
                                            parameterWithName("receiverId").description("상대방 id")
                                    )
                                    .responseFields(
                                            fieldWithPath("message").description("결과 메세지"),
                                            fieldWithPath("result").description("결과"),
                                            fieldWithPath("result.channelId").description("채팅 채널 id"),
                                            fieldWithPath("result.chatId").description("내 채팅방 id")
                                    )
                                    .build()
                    )
            ));
        }
    }

    @Nested
    @DisplayName("그룹 채널 개설")
    class startGroupChannel {
        Storage storage = Storage.builder()
                .id(1L)
                .name("창고")
                .location(geomUtil.createPoint(127.02, 37.49))
                .build();

        User channelOwner = User.builder()
                .id(2L)
                .userKey("test")
                .build();

        Item groupItem = Item.builder()
                .id(3L)
                .itemType(ItemType.GROUP_BUY)
                .name("공동구매 아이템")
                .description("")
                .user(channelOwner)
                .status(true)
                .imgUrl("")
                .viewCount(0)
                .storage(storage)
                .build();

//        StartGroupChannelRequestDto badRequestDto = StartGroupChannelRequestDto.builder()
//                .itemId(individualItem.getId())
//                .defaultTitle("공동구매")
//                .limit(5)
//                .build();

        StartGroupChannelRequestDto normalRequestDto = StartGroupChannelRequestDto.builder()
                .itemId(groupItem.getId())
                .defaultTitle("공동구매")
                .limit(5)
                .build();

        GroupChannel groupChannel = GroupChannel.builder()
                .id(3L)
                .channelType(ChannelType.GROUP)
                .owner(channelOwner)
                .item(groupItem)
                .build();
        Chat chat = Chat.builder()
                .id(3L)
                .title(normalRequestDto.getDefaultTitle())
                .channel(groupChannel)
                .build();

        @Test
        @DisplayName("그룹 채널 개설 실패 : 이미 채널이 존재하는 경우")
        void test1() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(channelOwner.getId());
            BDDMockito.given(channelServiceMock.createGroupChannel(channelOwner.getId(),
                            normalRequestDto.getItemId(), normalRequestDto.getDefaultTitle(), normalRequestDto.getLimit()))
                    .willReturn(new CreateChannelDto(false, groupChannel.getId(), chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/group")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(normalRequestDto))
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(channelServiceMock, times(1)).createGroupChannel(channelOwner.getId(),
                    normalRequestDto.getItemId(), normalRequestDto.getDefaultTitle(), normalRequestDto.getLimit());
            resultActions.andExpect(status().isConflict());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("이미 진행중인 채널이 있습니다.", new ChannelAndChatDto(groupChannel.getId(), chat.getId()))
            )));

        }

        @Test
        @Tag("api")
        @DisplayName("그룹 채널 개설 성공")
        void test2() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(channelOwner.getId());
            BDDMockito.given(channelServiceMock.createGroupChannel(anyLong(), anyLong(), anyString(), anyInt()))
                    .willReturn(new CreateChannelDto(true, groupChannel.getId(), chat.getId()));

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/channel/group")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(normalRequestDto))
                            .header("Authorization", "Bearer {accessToken}")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(channelServiceMock, times(1)).createGroupChannel(channelOwner.getId(),
                    normalRequestDto.getItemId(), normalRequestDto.getDefaultTitle(), normalRequestDto.getLimit());

            resultActions.andExpect(status().isCreated());
            resultActions.andExpect(content().json(objectMapper.writeValueAsString(
                    new CommonResponseDto<>("그룹 채널이 개설되었습니다.", new ChannelAndChatDto(groupChannel.getId(), chat.getId()))
                    ))
            );

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("그룹 채널 개설")
                                    .description("그룹 채널을 개설합니다. \n\n" +
                                            "공동 구매 아이템 id, 채널 기본 제목, 정원을 입력 받습니다.")
                                    .requestFields(
                                            fieldWithPath("itemId").description("그룹 채팅이 할당될 공동 구매 아이템 id"),
                                            fieldWithPath("defaultTitle").description("기본 설정 그룹 채널 제목"),
                                            fieldWithPath("limit").description("채팅 채널 정원")
                                    )
                                    .responseFields(
                                            fieldWithPath("message").description("그룹 채널 개설 결과 메세지"),
                                            fieldWithPath("result").description("해당 아이템의 Channel ID 와 Chat ID"),
                                            fieldWithPath("result.channelId").description("그룹 채널 ID"),
                                            fieldWithPath("result.chatId").description("내 채팅방 ID")
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
    @DisplayName("공동 구매 아이템에 등록된 그룹 채널 조회")
    class findGroupChannel {
        User owner = User.builder().id(1L).userKey("test").role(Role.USER).build();
        Item item = Item.builder().id(2L).itemType(ItemType.GROUP_BUY).status(true).build();
        GroupChannel groupChannel = GroupChannel.builder()
                .id(3L)
                .item(item)
                .channelType(ChannelType.GROUP)
                .owner(owner)
                .lastMessage("마지막으로 나눈 대화")
                .participantsCount(5)
                .channelLimit(10)
                .defaultTitle("채팅방 기본 제목")
                .build();
        @Test
        @DisplayName("조회 결과 없음")
        void test1() throws Exception {
            // given
            BDDMockito.given(channelServiceMock.findGroupChannel(item.getId())).willReturn(Optional.empty());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/channel/group/{itemId}", item.getId())
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("message").value("등록된 그룹 채널이 없습니다."));

        }

        @Test
        @DisplayName("조회 결과 있음")
        @Tag("api")
        void test2() throws Exception {
            // given
            GroupChannelDto groupChannelDto = new GroupChannelDto(groupChannel);
            BDDMockito.given(channelServiceMock.findGroupChannel(item.getId())).willReturn(Optional.of(groupChannelDto));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/channel/group/{itemId}", item.getId())
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("message").value("조회 성공"));

            // restDoc
            resultActions.andDo(restDocs.document(resource(
                    ResourceSnippetParameters.builder()
                            .tag("채팅")
                            .summary("그룹 채널 조회")
                            .description("공동 구매 아이템에 등록된 그룹 채널의 정보를 조회해 옵니다.")
                            .pathParameters(
                                    parameterWithName("itemId").description("아이템 ID")
                            )
                            .responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("result").description("조회 결과"),
                                    fieldWithPath("result.channelId").description("채널 ID"),
                                    fieldWithPath("result.ownerId").description("아이템 주인 ID"),
                                    fieldWithPath("result.itemId").description("아이템 ID"),
                                    fieldWithPath("result.participantsCount").description("채널 참여자 수"),
                                    fieldWithPath("result.defaultTitle").description("채널 제목"),
                                    fieldWithPath("result.channelLimit").description("채널 정원")
                            ).build()
                    )
            ));
        }
    }

    @Nested
    @DisplayName("근처 그룹 채널 조회")
    class findGroupChannelNearby {
        User user = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();

        Storage storage = Storage.builder()
                .id(10L)
                .name("테스트 창고1")
                .location(geomUtil.createPoint(127.0368, 37.4949))
                .address("서울시 강남구")
                .build();

        Item item1 = Item.builder()
                .id(2L)
                .itemType(ItemType.GROUP_BUY)
                .name("테스트 아이템1")
                .description("")
                .status(true)
                .imgUrl("")
                .viewCount(0)
                .storage(storage)
                .build();

        Item item2 = Item.builder()
                .id(3L)
                .itemType(ItemType.GROUP_BUY)
                .name("테스트 아이템2")
                .description("")
                .status(true)
                .imgUrl("")
                .viewCount(0)
                .storage(storage)
                .build();

        GroupChannel channel1 = GroupChannel.builder()
                .id(4L)
                .channelType(ChannelType.GROUP)
                .item(item1)
                .participantsCount(3)
                .channelLimit(5)
                .owner(user)
                .defaultTitle("그룹채널 1")
                .build();

        GroupChannel channel2 = GroupChannel.builder()
                .id(5L)
                .channelType(ChannelType.GROUP)
                .item(item2)
                .participantsCount(2)
                .channelLimit(10)
                .owner(user)
                .defaultTitle("그룹채널 2")
                .build();

        int radius = 2000;
        Coord coord = new Coord(127.037422, 37.4954475);

        @Test
        @DisplayName("조회 결과 없음")
        void test1() throws Exception {
            // given
            BDDMockito.given(channelServiceMock.findGroupChannelNearby(coord, radius, 0, 10)).willReturn(new ArrayList<>());

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
                    new CommonResponseDto<>("근처에 진행중인 그룹 채팅이 없습니다.", new ArrayList<>()))));
        }

        @Test
        @Tag("api")
        @DisplayName("조회 결과 있음")
        void test2() throws Exception {
            // given
            List<GroupChannel> groupChannels = Arrays.asList(channel1, channel2);
            List<GroupChannelDto> dtos = groupChannels.stream().map(GroupChannelDto::new).collect(Collectors.toList());

            BDDMockito.given(channelServiceMock.findGroupChannelNearby(coord, radius, 0, 10)).willReturn(dtos);

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
                    new CommonResponseDto<>("가까운 그룹 채널이 조회 되었습니다", dtos))));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("채팅")
                            .summary("근처 그룹 채널 조회")
                            .description("근처의 그룹 채널 정보를 조회합니다.")
                            .requestParameters(
                                    parameterWithName("lon").description("조회하고자 하는 중심 경도"),
                                    parameterWithName("lat").description("조회하고자 하는 중심 위도"),
                                    parameterWithName("rad").description("조회 반경").defaultValue("1000"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("result[]").description("조회한 그룹 채널 정보"),
                                    fieldWithPath("result[].channelId").description("채널 id"),
                                    fieldWithPath("result[].itemId").description("공동 구매 아이템 id"),
                                    fieldWithPath("result[].ownerId").description("채널 주인(창고 주인)"),
                                    fieldWithPath("result[].participantsCount").description("채널 참여자 수"),
                                    fieldWithPath("result[].defaultTitle").description("채널 제목"),
                                    fieldWithPath("result[].channelLimit").description("채널 정원")
                            ).responseSchema(
                                    Schema.schema("근처 그룹 채널 조회 Response")
                            ).build()
                    )));
        }
    }

    @Nested
    @DisplayName("채널에 참여하고 있는 유저 조회")
    class findChannelParticipants {
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
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(participant1.getId());
            BDDMockito.given(channelServiceMock.findParticipantsInChannel(groupChannel.getId(), participant1.getId())).willReturn(infoListDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/channel/{channelId}/participants", groupChannel.getId())
                            .with(SecurityMockMvcRequestPostProcessors.csrf()));

            // then
            verify(authenticationHandlerMock, times(1)).getUserId(any());
            verify(channelServiceMock, times(1)).findParticipantsInChannel(groupChannel.getId(), participant1.getId());

            resultActions.andExpect(status().isOk());
            resultActions.andExpect(content()
                    .json(objectMapper.writeValueAsString(
                            new CommonResponseDto<>("채널 참여자 정보를 조회하였습니다.", infoListDto)
                    ))
            );

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("채팅")
                                    .summary("채널 참여자 조회")
                                    .description("특정 채널에 참여한 참여자 정보를 조회합니다.")
                                    .pathParameters(
                                            parameterWithName("channelId").description("참여자 조회를 하고자 하는 채널 id")
                                    ).responseFields(
                                            fieldWithPath("message").description("조회 결과 메세지"),
                                            fieldWithPath("result[]").description("채널 참여자 정보"),
                                            fieldWithPath("result[].participantId").description("참여자 id"),
                                            fieldWithPath("result[].nickname").description("참여자 닉네임"),
                                            fieldWithPath("result[].imgUrl").description("참여자 사진 링크")
                                    ).responseSchema(
                                            Schema.schema("채널 참여자 조회 Response")
                                    ).build()
                    )
            ));
        }
    }

    @Nested
    @DisplayName("채널 종료")
    class closeChannel {
        @Test
        @Tag("api")
        @DisplayName("종료 성공")
        void test() throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(2L);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/channel/{channelId}", 1L)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(channelServiceMock).closeChannel(1L, 2L);
            resultActions.andExpect(status().isOk());

            // restDocs
            resultActions.andDo(restDocs.document(resource(
                    ResourceSnippetParameters.builder()
                            .tag("채팅")
                            .summary("채널 종료")
                            .description("채널을 종료 합니다. \n\n" +
                                    "채널에 참여할 수 없고, 메세지를 주고 받지 못합니다. \n\n" +
                                    "그룹 채팅의 경우 근처 채널 조회에서 조회되지 않습니다.")
                            .responseFields(
                                    fieldWithPath("message").description("메세지"),
                                    fieldWithPath("result").description("null")
                            )
                    .build())));

        }
    }



}