package com.example.naejango.domain.storage.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.domain.ChatType;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.SearchStorageResultDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorageController.class)
class StorageControllerTest extends RestDocsSupportTest {

    @MockBean
    private StorageService storageServiceMock;
    @MockBean
    private StorageRepository storageRepositoryMock;
    @MockBean
    private AuthenticationHandler authenticationHandlerMock;
    @MockBean
    private CategoryRepository categoryRepositoryMock;
    @MockBean
    private ChannelRepository channelRepository;
    @MockBean
    private GeomUtil geomUtilMock;
    private final GeomUtil geomUtil = new GeomUtil();

    @Test
    @Tag("api")
    @DisplayName("창고 생성")
    void createStorageTest() throws Exception {
        //given
        double testLon = 126.0;
        double testLat = 37.0;
        Coord testCoord = new Coord(testLon, testLat);
        CreateStorageRequestDto requestDto = new CreateStorageRequestDto("name", testCoord,
                "address", "description", "imgUrl");

        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .post("/api/storage")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "엑세스 토큰")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(authenticationHandlerMock, only()).userIdFromAuthentication(any());
        verify(storageServiceMock, only()).createStorage(anyString(), any(Coord.class), anyString(), anyString(), anyString(), anyLong());
        resultActions.andExpect(
                status().isCreated());

        // RestDocs
        resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .description("창고 생성하여 내 창고 목록에 추가")
                                        .requestFields(
                                                fieldWithPath("name").description("이름"),
                                                fieldWithPath("imgUrl").description("이미지 링"),
                                                fieldWithPath("description").description("창고 소개"),
                                                fieldWithPath("address").description("주소"),
                                                fieldWithPath("coord").description("좌표"),
                                                fieldWithPath("coord.longitude").description("경도"),
                                                fieldWithPath("coord.latitude").description("위도")
                                        )
                                        .responseFields(
                                                fieldWithPath("storageId").description("생성된 창고 id"),
                                                fieldWithPath("message").description("결과 메세지")
                                        )
                                        .requestSchema(
                                                Schema.schema("창고 생성 Request")
                                        ).responseSchema(
                                                Schema.schema("창고 생성 Response")
                                        ).build()
                        )));
    }

    @Test
    @Tag("api")
    @DisplayName("요청 회원의 창고 목록 조회")
    void storageList() throws Exception {
        // when
        Storage testStorage1 = Storage.builder().id(1L).name("창고1").imgUrl("imgUrl")
                .description("집").address("강남")
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();
        Storage testStorage2 = Storage.builder().id(2L).name("창고2").imgUrl("imgUrl")
                .description("회사").address("서초")
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();
        Storage testStorage3 = Storage.builder().id(3L).name("창고3").imgUrl("imgUrl")
                .description("본가").address("구로")
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();

        List<Storage> storages = Arrays.asList(testStorage1, testStorage2, testStorage3);
        BDDMockito.given(storageServiceMock.myStorageList(anyLong())).willReturn(storages);

        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/storage")
                .header("Authorization", "엑세스 토큰")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(authenticationHandlerMock, only()).userIdFromAuthentication(any());
        verify(storageServiceMock, only()).myStorageList(anyLong());
        resultActions.andExpect(
                status().isOk()
        );

        // RestDocs
        resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .description("요청 유저의 보유 창고 목록을 조회")
                                        .responseFields(
                                                fieldWithPath("storageList[]").type(JsonFieldType.ARRAY).description("StorageInfoDto 객체 목록"),
                                                fieldWithPath("storageList[].id").description("창고 id"),
                                                fieldWithPath("storageList[].name").description("창고 이름"),
                                                fieldWithPath("storageList[].imgUrl").description("창고 이미지 링크"),
                                                fieldWithPath("storageList[].description").description("창고 소개"),
                                                fieldWithPath("storageList[].address").description("창고 주소"),
                                                fieldWithPath("storageList[].coord").description("창고 좌표"),
                                                fieldWithPath("storageList[].coord.longitude").description("경도"),
                                                fieldWithPath("storageList[].coord.latitude").description("위도"),
                                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("보유 창고 개수")
                                        )
                                        .responseSchema(
                                                Schema.schema("내 창고 조회 시 응답")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Nested
    @Tag("api")
    @DisplayName("창고 검색")
    class SearchStorageByConditions {

        @Test
        @DisplayName("모든 조건으로 창고 검색")
        void test1() throws Exception {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category cat1 = new Category(1, "의류");
            Integer rad = 1000;
            Integer page = 0;
            Integer size = 10;
            Storage testStorage1 = Storage.builder().name("테스트1").location(geomUtil.createPoint(127.021, 37.491)).address("").build();
            Storage testStorage2 = Storage.builder().name("테스트2").location(geomUtil.createPoint(127.022, 37.492)).address("").build();
            SearchStorageResultDto result1 = new SearchStorageResultDto(testStorage1, 100);
            SearchStorageResultDto result2 = new SearchStorageResultDto(testStorage2, 200);


            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{"%유니클로%", "%청바지%"}, ItemType.BUY, true);
            BDDMockito.given(geomUtilMock.createPoint(127.02, 37.49)).willReturn(center);
            BDDMockito.given(categoryRepositoryMock.findById(1)).willReturn(Optional.of(cat1));
            BDDMockito.given(storageRepositoryMock.searchStorageByConditions(center, rad, page, size, conditions))
                    .willReturn(Arrays.asList(result1, result2));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/storage/search")
                    .queryParam("lon", "127.02")
                            .queryParam("lat", "37.49")
                            .queryParam("rad","1000")
                            .queryParam("page", "0")
                            .queryParam("size", "10")
                            .queryParam("cat", "1")
                            .queryParam("keyword", "유니클로 청바지")
                            .queryParam("type", "BUY")
                            .queryParam("status", "true")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .header("Authorization", "엑세스 토큰")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(geomUtilMock, times(1)).createPoint(127.02, 37.49);
            verify(categoryRepositoryMock, times(1)).findById(1);
            verify(storageRepositoryMock, times(1)).searchStorageByConditions(center, rad, page, size, conditions);

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("창고")
                            .summary("창고 검색")
                            .description("조건에 맞는 창고를 검색합니다.\n\n" +
                                    "좌표, 반경, 카테고리, 키워드, 타입, 상태를 조건으로 받습니다.\n\n" +
                                    "창고 정보만 응답합니다. 추후 아이템 정보도 가지고 올 수 있도록 수정하겠습니다.")
                            .requestParameters(
                                    parameterWithName("lon").description("중심 경도 좌표"),
                                    parameterWithName("lat").description("중심 위도 좌표"),
                                    parameterWithName("rad").description("반경 (1,000~5,000m)"),
                                    parameterWithName("page").description("페이지"),
                                    parameterWithName("size").description("사이즈"),
                                    parameterWithName("cat").description("카테고리ID"),
                                    parameterWithName("keyword").description("검색 키워드(2~10자)"),
                                    parameterWithName("type").description("BUY/SELL"),
                                    parameterWithName("status").description("상태 (true/false)"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("coord").description("중심 좌표"),
                                    fieldWithPath("coord.longitude").description("중심 경도 좌표"),
                                    fieldWithPath("coord.latitude").description("중심 위도 좌표"),
                                    fieldWithPath("radius").description("반경"),
                                    fieldWithPath("page").description("페이지"),
                                    fieldWithPath("size").description("조회 결과물 수"),
                                    fieldWithPath("searchingConditions").description("검색 조건"),
                                    fieldWithPath("searchingConditions.cat").description("검색 조건 : 카테고리"),
                                    fieldWithPath("searchingConditions.cat.id").description("카테고리 아이디"),
                                    fieldWithPath("searchingConditions.cat.name").description("카테고리 이름"),
                                    fieldWithPath("searchingConditions.keyword[]").description("검색 조건 : 키워드"),
                                    fieldWithPath("searchingConditions.itemType").description("검색 조건 : 상품 타입"),
                                    fieldWithPath("searchingConditions.status").description("검색 조건 : 상품 상태"),
                                    fieldWithPath("searchResult[].storageId").description("창고 Id"),
                                    fieldWithPath("searchResult[].name").description("창고 이름"),
                                    fieldWithPath("searchResult[].imgUrl").description("이미지 링크"),
                                    fieldWithPath("searchResult[].coord").description("창고 좌표"),
                                    fieldWithPath("searchResult[].address").description("창고 주소"),
                                    fieldWithPath("searchResult[].distance").description("거리")
                            ).requestSchema(
                                    Schema.schema("창고 검색 Request")
                            ).responseSchema(
                                    Schema.schema("창고 검색 Response")
                            )
                            .build())
            ));
        }
    }




    @Test
    @Tag("api")
    @DisplayName("창고의 아이템 조회")
    void findItems() throws Exception {
        // given
        Storage storage = Storage.builder().id(1L).name("테스트 창고").build();
        Item item1 = Item.builder().id(2L).status(true).type(ItemType.BUY).name("item1").imgUrl("imgUrl").description("아이템 설명").build();
        Item item2 = Item.builder().id(3L).status(true).type(ItemType.SELL).name("item2").imgUrl("imgUrl").description("아이템 설명").build();
        Item item3 = Item.builder().id(4L).status(false).type(ItemType.BUY).name("item3").imgUrl("imgUrl").description("아이템 설명").build();
        Item item4 = Item.builder().id(5L).status(true).type(ItemType.SELL).name("item4").imgUrl("imgUrl").description("아이템 설명").build();
        Item item5 = Item.builder().id(6L).status(true).type(ItemType.SELL).name("item5").imgUrl("imgUrl").description("아이템 설명").build();
        Category category = Category.builder().id(7).name("생필품").build();
        List<Item> itemList = List.of(item1, item2, item3, item4, item5);
        List<ItemInfoDto> ItemInfoList = itemList.stream().filter(Item::getStatus)
                .map(item -> new ItemInfoDto(item, category.getName())).collect(Collectors.toList());

        // when
        BDDMockito.given(storageServiceMock.findItemList(storage.getId(), true, 0, 10)).willReturn(ItemInfoList);
        BDDMockito.given(storageServiceMock.findUserIdByStorageId(storage.getId())).willReturn(1L);
        // then
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/storage/{storageId}/items", 1L)
                .queryParam("status", "true")
                .queryParam("page", "0")
                .queryParam("size", "10")
        );

        // then
        verify(storageServiceMock, times(1)).findItemList(1L, Boolean.TRUE, 0, 10);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("창고 내의 아이템을 조회했습니다."))
                .andExpect(jsonPath("page").value("0"))
                .andExpect(jsonPath("size").value("10"));

        // restDocs
        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("창고")
                                .description("특정 창고의 아이템 목록을 조회")
                                .pathParameters(
                                        parameterWithName("storageId").description("창고 id")
                                )
                                .requestParameters(
                                        parameterWithName("status").description("아이템의 상태값 (true=거래중, false=거래완료)"),
                                        parameterWithName("page").description("요청 페이지(0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 결과물 수")
                                )
                                .responseFields(
                                        fieldWithPath("message").description("조회 결과 메세지"),
                                        fieldWithPath("page").description("요청한 페이지"),
                                        fieldWithPath("size").description("페이지당 결과물 수"),
                                        fieldWithPath("userId").description("창고의 유저 ID"),
                                        fieldWithPath("itemList[].itemId").description("아이템 id"),
                                        fieldWithPath("itemList[].category").description("아이템 카테고리"),
                                        fieldWithPath("itemList[].type").description("아이템 타입(BUY / SELL)"),
                                        fieldWithPath("itemList[].name").description("아이템 제목"),
                                        fieldWithPath("itemList[].imgUrl").description("아이템 이미지 링크"),
                                        fieldWithPath("itemList[].description").description("아이템 설명")
                                )
                                .responseSchema(
                                        Schema.schema("아이템 조회 응답")
                                )
                                .build()
                )));
    }

    @Test
    @Tag("api")
    @DisplayName("창고의 그룹 채널 조회")
    void findGroupChannel() throws Exception {
        // given
        User user = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();

        Storage storage = Storage.builder()
                .id(2L)
                .name("테스트 창고1")
                .location(geomUtil.createPoint(127.0371, 37.4951))
                .address("서울시 강남구")
                .build();

        GroupChannel channel = GroupChannel.builder()
                .id(4L)
                .chatType(ChatType.GROUP)
                .storageId(storage.getId())
                .participantsCount(3)
                .channelLimit(5)
                .ownerId(user.getId())
                .defaultTitle("그룹채널 1")
                .build();

        BDDMockito.given(channelRepository.findGroupChannelByStorageId(storage.getId()))
                .willReturn(Optional.of(channel));

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/storage/{storageId}/channel", storage.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("message").value("해당 창고의 그룹 채널 정보가 조회되었습니다."));

        // restDocs
        resultActions.andDo(restDocs.document(
                resource(ResourceSnippetParameters.builder()
                        .tag("창고")
                        .summary("창고 그룹 채널 조회")
                        .pathParameters(
                                parameterWithName("storageId").description("창고 id")
                        ).responseFields(
                                fieldWithPath("message").description("조회 결과 메세지"),
                                fieldWithPath("channelInfo").description("조회된 채널"),
                                fieldWithPath("channelInfo.channelId").description("조회된 채널"),
                                fieldWithPath("channelInfo.ownerId").description("조회된 채널"),
                                fieldWithPath("channelInfo.storageId").description("조회된 채널"),
                                fieldWithPath("channelInfo.participantsCount").description("조회된 채널"),
                                fieldWithPath("channelInfo.defaultTitle").description("조회된 채널"),
                                fieldWithPath("channelInfo.channelLimit").description("조회된 채널")
                        ).responseSchema(
                                Schema.schema("창고 채널 조회 Response")
                        )
                        .build())
        ));
    }

    @Test
    @Tag("api")
    @DisplayName("창고 정보 수정")
    void modifyStorageInfoTest() throws Exception {
        // given
        Storage testStorage = Storage.builder().id(1L).name("창고1").imgUrl("imgUrl")
                .description("집").address("강남")
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();

        ModifyStorageInfoRequestDto requestDto = ModifyStorageInfoRequestDto.builder().name("변경된 이름").description("창고 정보 변경").imgUrl("변경 url").build();

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .patch("/api/storage/{storageId}", testStorage.getId())
                .header("Authorization", "엑세스 토큰")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        verify(storageServiceMock, times(1)).modifyStorageInfo(any(ModifyStorageInfoRequestDto.class), anyLong(), anyLong());

        resultActions.andExpect(status().isOk());

        // RestDocs
        resultActions.andDo(
                restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .description("창고 정보 수정")
                                        .requestFields(
                                                fieldWithPath("name").description("창고 이름"),
                                                fieldWithPath("imgUrl").description("창고 이미지 url"),
                                                fieldWithPath("description").description("창고 소개")
                                        )
                                        .requestSchema(
                                                Schema.schema("창고 수정 dto")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @Tag("api")
    @DisplayName("창고 삭제")
    void deleteStorageTest() throws Exception {
        // given
        Storage storage = Storage.builder().id(1L).name("창고1").imgUrl("imgUrl")
                .description("집").address("강남")
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .delete("/api/storage/{storageId}", storage.getId())
                .header("Authorization", "엑세스 토큰")
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        verify(storageServiceMock, times(1)).deleteStorage(anyLong(), anyLong());

        // RestDocs
         resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .description("창고 삭제")
                                        .build()
                        )
                )
        );
    }

}