package com.example.naejango.domain.storage.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyInfo;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import com.example.naejango.global.common.handler.GeomUtil;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(StorageController.class)
class StorageControllerTest extends RestDocsSupportTest {
    @MockBean
    private StorageService storageServiceMock;
    @MockBean
    private CommonDtoHandler commonDtoHandlerMock;
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
        CreateStorageRequestDto requestDto =
                new CreateStorageRequestDto("name", "imgUrl", "description", "address", testCoord);

        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/storage")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "엑세스 토큰")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(commonDtoHandlerMock, only()).userIdFromAuthentication(any());
        verify(storageServiceMock, only()).createStorage(any(CreateStorageRequestDto.class), any(Long.class));
        resultActions.andExpect(
                MockMvcResultMatchers
                        .status().isCreated());

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
                                        .responseFields()
                                        .requestSchema(
                                                Schema.schema("창고 생성 Request")
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

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/storage")
                        .header("Authorization", "엑세스 토큰")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(commonDtoHandlerMock, only()).userIdFromAuthentication(any());
        verify(storageServiceMock, only()).myStorageList(anyLong());
        resultActions.andExpect(
                MockMvcResultMatchers
                        .status().isOk()
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

    @Test
    @Tag("api")
    @DisplayName("좌표 및 반경을 기준으로 창고 조회 (거리 순 정렬 및 페이징 처리)")
    void storageNearbyTest() throws Exception {
        // given
        String centerLongitude ="126.0";
        String centerLatitude = "37.0";
        double centerLon = Double.parseDouble(centerLongitude);
        double centerLat = Double.parseDouble(centerLatitude);
        Point center = geomUtil.createPoint(centerLon, centerLat);

        int rad = 1000;
        int limit = 10;
        int page = 1;

        String longitude1 ="126.00001";
        String latitude1 = "37.00001";
        double lon1 = Double.parseDouble(longitude1);
        double lat1 = Double.parseDouble(latitude1);
        Point testLocation1 = geomUtil.createPoint(lon1, lat1);

        String longitude2 ="126.00002";
        String latitude2 = "37.00002";
        double lon2 = Double.parseDouble(longitude2);
        double lat2 = Double.parseDouble(latitude2);
        Point testLocation2 = geomUtil.createPoint(lon2, lat2);

        Storage testStorage1 = Storage.builder().id(1L).name("test1").location(testLocation1).address("address1").build();
        Storage testStorage2 = Storage.builder().id(2L).name("test2").location(testLocation2).address("address2").build();
        StorageNearbyInfo storageNearbyInfo1 = new StorageNearbyInfo(testStorage1, geomUtil.calculateDistance(center, testLocation1));
        StorageNearbyInfo storageNearbyInfo2 = new StorageNearbyInfo(testStorage2, geomUtil.calculateDistance(center, testLocation2));

        List<StorageNearbyInfo> content = new ArrayList<>(Arrays.asList(storageNearbyInfo1, storageNearbyInfo2));

        BDDMockito.given(geomUtilMock.createPoint(anyDouble(), anyDouble())).willReturn(center);
        BDDMockito.given(storageServiceMock.countStorageNearby(any(Point.class), anyInt())).willReturn(2);
        BDDMockito.given(storageServiceMock.storageNearby(any(Point.class), anyInt(), anyInt(), anyInt())).willReturn(content);
        BDDMockito.given(geomUtilMock.calculateDistance(any(Point.class), any(Point.class))).willReturn(0);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/storage/nearby")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .queryParam("lon", centerLongitude)
                        .queryParam("lat", centerLatitude)
                        .queryParam("rad", String.valueOf(rad))
                        .queryParam("limit", String.valueOf(limit))
                        .queryParam("page", String.valueOf(page))
                        .header("Authorization", "엑세스 토큰")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(geomUtilMock, times(1)).createPoint(126.0, 37.0);
        verify(storageServiceMock, times(1)).countStorageNearby(center, rad);
        verify(storageServiceMock, times(1)).storageNearby(center, rad, limit, page);

        resultActions.andExpect(
                MockMvcResultMatchers
                        .status().isOk()
        );

        // RestDocs
        resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .description("근처 창고 목록 조회")
                                        .requestParameters(
                                                parameterWithName("lon").description("경도"),
                                                parameterWithName("lat").description("위도"),
                                                parameterWithName("rad").description("반경"),
                                                parameterWithName("limit").description("페이지 당 조회 창고 수"),
                                                parameterWithName("page").description("요청 페이지"),
                                                parameterWithName("_csrf").ignored()
                                        )
                                        .responseFields(
                                                fieldWithPath("content[]").description("창고 목록"),
                                                fieldWithPath("content[].id").description("창고 id"),
                                                fieldWithPath("content[].name").description("창고 이름"),
                                                fieldWithPath("content[].imgUrl").description("창고 이미지 링크"),
                                                fieldWithPath("content[].description").description("창고 소개"),
                                                fieldWithPath("content[].address").description("창고 주소"),
                                                fieldWithPath("content[].coord").description("창고 좌표"),
                                                fieldWithPath("content[].coord.longitude").description("경도"),
                                                fieldWithPath("content[].coord.latitude").description("위도"),
                                                fieldWithPath("content[].distance").description("중심과의 거리"),
                                                fieldWithPath("page").description("조회 페이지"),
                                                fieldWithPath("size").description("조회 창고 수"),
                                                fieldWithPath("totalCount").description("총 조회 창고 수"),
                                                fieldWithPath("totalPage").description("총 페이지 수")
                                        )
                                        .requestSchema(
                                                Schema.schema("근처 창고 조회 요청")
                                        )
                                        .responseSchema(
                                                Schema.schema("근처 창고 조회 결과")
                                        ).build()
                        )));
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
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .patch("/api/storage/{storageId}", testStorage.getId())
                        .header("Authorization", "엑세스 토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(storageServiceMock, times(1)).modifyStorageInfo(any(ModifyStorageInfoRequestDto.class), anyLong(), anyLong());

        resultActions.andExpect(
                MockMvcResultMatchers.status().isOk()
        );

        // RestDocs
        resultActions.andDo(
                restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
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
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .delete("/api/storage/{storageId}", storage.getId())
                        .header("Authorization", "엑세스 토큰")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(storageServiceMock, times(1)).deleteStorage(anyLong(), anyLong());

        // RestDocs
         resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("창고")
                                        .build()
                        )
                )
        );
    }
}