package com.example.naejango.domain.storage.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.StorageInfoDto;
import com.example.naejango.domain.storage.dto.StorageInfoWithDistanceDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.request.SearchStorageRequestDto;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private AuthenticationHandler authenticationHandlerMock;
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
        verify(authenticationHandlerMock, only()).getUserId(any());
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
                                                fieldWithPath("result").description("생성된 창고 id"),
                                                fieldWithPath("message").description("결과 메세지")
                                        )
                                        .requestSchema(
                                                Schema.schema("창고 생성 Request")
                                        ).responseSchema(
                                                Schema.schema("창고 생성 Response")
                                        ).build()
                        )));
    }

    @Nested
    @DisplayName("요청 회원의 창고 목록 조회")
    class MyStorageList {
        User user = User.builder().id(5L).build();
        Storage testStorage1 = Storage.builder().id(1L).name("창고1").imgUrl("imgUrl")
                .description("집").address("강남").user(user)
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();
        Storage testStorage2 = Storage.builder().id(2L).name("창고2").imgUrl("imgUrl")
                .description("회사").address("서초").user(user)
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();
        Storage testStorage3 = Storage.builder().id(3L).name("창고3").imgUrl("imgUrl")
                .description("본가").address("구로").user(user)
                .location(geomUtil.createPoint(12.12, 34.34))
                .build();

        @Test
        @Tag("api")
        @DisplayName("요청 회원의 창고 목록 조회")
        void storageList() throws Exception {
            // when
            List<Storage> storages = Arrays.asList(testStorage1, testStorage2, testStorage3);
            BDDMockito.given(storageServiceMock.myStorageList(anyLong()))
                    .willReturn(storages.stream().map(StorageInfoDto::new).collect(Collectors.toList()));

            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/storage")
                    .header("Authorization", "엑세스 토큰")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(authenticationHandlerMock, only()).getUserId(any());
            verify(storageServiceMock, only()).myStorageList(anyLong());
            resultActions.andExpect(
                    status().isOk()
            );

            // RestDocs
            resultActions.andDo(restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("창고")
                                            .summary("내 창고 목록 조회")
                                            .description("요청 유저의 보유 창고 목록을 조회")
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result[]").type(JsonFieldType.ARRAY).description("조회 결과"),
                                                    fieldWithPath("result[].storageId").description("창고 ID"),
                                                    fieldWithPath("result[].ownerId").description("창고 소유 회원 ID"),
                                                    fieldWithPath("result[].name").description("창고 이름"),
                                                    fieldWithPath("result[].imgUrl").description("창고 이미지 링크"),
                                                    fieldWithPath("result[].description").description("창고 소개"),
                                                    fieldWithPath("result[].address").description("창고 주소"),
                                                    fieldWithPath("result[].coord").description("창고 좌표"),
                                                    fieldWithPath("result[].coord.longitude").description("경도"),
                                                    fieldWithPath("result[].coord.latitude").description("위도")
                                            )
                                            .responseSchema(
                                                    Schema.schema("내 창고 조회 시 응답")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("근처의 창고 검색")
    class StorageNearby {
        User user = User.builder().id(10L).build();
        Storage storage1 = Storage.builder().id(1L).name("창고 1").imgUrl("이미지").description("테스트 창고 입니다.")
                .user(user).address("서울시 강남구").location(geomUtil.createPoint(127.021, 37.493)).build();

        Storage storage2 = Storage.builder().id(1L).name("창고 2").imgUrl("이미지").description("테스트 창고 입니다.")
                .user(user).address("서울시 강남구").location(geomUtil.createPoint(127.025, 37.492)).build();

        Storage storage3 = Storage.builder().id(1L).name("창고 2").imgUrl("이미지").description("테스트 창고 입니다.")
                .user(user).address("서울시 강남구").location(geomUtil.createPoint(127.03, 37.48)).build();
        Point center = geomUtil.createPoint(127.02, 37.49);
        @Test
        @DisplayName("검색 성공")
        void test1() throws Exception {
            // given
            SearchStorageRequestDto requestDto = SearchStorageRequestDto.builder().lon(127.02).lat(37.49).page(0).size(10).rad(1000).build();

            BDDMockito.given(geomUtilMock.createPoint(requestDto.getLon(), requestDto.getLat()))
                    .willReturn(center);
            BDDMockito.given(storageServiceMock.searchStorage(center,
                            requestDto.getRad(), requestDto.getPage(), requestDto.getSize()))
                    .willReturn(Stream.of(storage1, storage2, storage3).map(s ->
                            new StorageInfoWithDistanceDto(s, geomUtil.calculateDistance(center, s.getLocation()))).collect(Collectors.toList()));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/storage/nearby")
                    .queryParam("lon", "127.02")
                    .queryParam("lat", "37.49")
                    .queryParam("rad", "1000")
                    .queryParam("page", "0")
                    .queryParam("size", "10")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(geomUtilMock, times(1)).createPoint(requestDto.getLon(), requestDto.getLat());
            verify(storageServiceMock, times(1)).searchStorage(center,
                    requestDto.getRad(), requestDto.getPage(), requestDto.getSize());

            resultActions.andExpect(status().isOk());

            // restDocs
            resultActions.andDo(restDocs.document(resource(
                    ResourceSnippetParameters.builder()
                            .tag("창고")
                            .summary("근처 창고 조회")
                            .description("주어진 좌표 및 반경에 따라 창고를 조회합니다.")
                            .requestParameters(
                                    parameterWithName("lon").description("경도"),
                                    parameterWithName("lat").description("위도"),
                                    parameterWithName("rad").description("반경"),
                                    parameterWithName("page").description("조회 페이지"),
                                    parameterWithName("size").description("조회 결과물 수"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회결과 메세지"),
                                    fieldWithPath("result[]").description("조회된 창고 리스트"),
                                    fieldWithPath("result[].storageId").description("창고 id"),
                                    fieldWithPath("result[].ownerId").description("창고를 소유한 유저 id"),
                                    fieldWithPath("result[].name").description("창고 이름"),
                                    fieldWithPath("result[].imgUrl").description("창고 이미지 url"),
                                    fieldWithPath("result[].description").description("창고 소개"),
                                    fieldWithPath("result[].address").description("창고 주소"),
                                    fieldWithPath("result[].coord").description("창고 좌표"),
                                    fieldWithPath("result[].coord.longitude").description("창고 경도 좌표"),
                                    fieldWithPath("result[].coord.latitude").description("창고 위도 좌표"),
                                    fieldWithPath("result[].distance").description("좌표로 부터의 거리")
                            ).responseSchema(
                                    Schema.schema("근처 창고 조회 Response")
                            ).build()
                    ))
            );

        }
    }


    @Nested
    @DisplayName("창고에 속한 아이템 조회")
    class ItemList {
        Storage storage = Storage.builder().id(1L).name("테스트 창고").build();
        Category category = Category.builder().id(1).name("생필품").build();
        User user = User.builder().id(7L).build();
        Item item1 = Item.builder().id(2L).status(true).itemType(ItemType.INDIVIDUAL_BUY).name("item1").imgUrl("imgUrl").description("아이템 설명")
                .user(user).category(category).build();
        Item item2 = Item.builder().id(3L).status(true).itemType(ItemType.INDIVIDUAL_SELL).name("item2").imgUrl("imgUrl").description("아이템 설명")
                .user(user).category(category).build();
        Item item3 = Item.builder().id(4L).status(false).itemType(ItemType.INDIVIDUAL_BUY).name("item3").imgUrl("imgUrl").description("아이템 설명")
                .user(user).category(category).build();
        Item item4 = Item.builder().id(5L).status(true).itemType(ItemType.INDIVIDUAL_SELL).name("item4").imgUrl("imgUrl").description("아이템 설명")
                .user(user).category(category).build();
        Item item5 = Item.builder().id(6L).status(true).itemType(ItemType.INDIVIDUAL_SELL).name("item5").imgUrl("imgUrl").description("아이템 설명")
                .user(user).category(category).build();
        List<Item> itemList = List.of(item1, item2, item3, item4, item5);
        @Test
        @Tag("api")
        @DisplayName("조회 성공")
        void test1() throws Exception {
            // given
            BDDMockito.given(storageServiceMock.findItemList(storage.getId(), true, 0, 10))
                    .willReturn(new PageImpl<>(itemList, PageRequest.of(0, 10), itemList.size()));
            BDDMockito.given(storageServiceMock.findUserIdByStorageId(storage.getId())).willReturn(1L);

            // when
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
                    .andExpect(jsonPath("message").value("창고의 아이템 조회 성공"));

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
                                            fieldWithPath("result[].itemId").description("아이템 id"),
                                            fieldWithPath("result[].ownerId").description("아이템 소유 회원 id"),
                                            fieldWithPath("result[].category").description("아이템 카테고리"),
                                            fieldWithPath("result[].itemType").description("아이템 타입(BUY / SELL)"),
                                            fieldWithPath("result[].name").description("아이템 제목"),
                                            fieldWithPath("result[].imgUrl").description("아이템 이미지 링크"),
                                            fieldWithPath("result[].description").description("아이템 설명")
                                    )
                                    .responseSchema(
                                            Schema.schema("아이템 조회 응답")
                                    )
                                    .build()
                    )));
        }
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
        verify(storageServiceMock, times(1)).modifyStorageInfo(anyLong(), anyLong(), anyString(), anyString(), anyString());

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