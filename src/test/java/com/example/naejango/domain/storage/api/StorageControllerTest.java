package com.example.naejango.domain.storage.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestServiceDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import com.example.naejango.global.common.handler.GeomUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    @DisplayName("createStorage: 창고 생성")
    void createStorageTest() throws Exception {
        //given
        double testLon = 126.0;
        double testLat = 37.0;
        CreateStorageRequestDto requestDto =
                new CreateStorageRequestDto("name", "imgUrl", "description", "address", testLon, testLat);

        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .post("http://localhost:8080/api/storage/")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(commonDtoHandlerMock, only()).userIdFromAuthentication(any());
        verify(geomUtilMock, only()).createPoint(anyDouble(), anyDouble());
        verify(storageServiceMock, only()).createStorage(any(CreateStorageRequestServiceDto.class), any(Long.class));
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
                                                fieldWithPath("longitude").description("경도"),
                                                fieldWithPath("latitude").description("위도")
                                        )
                                        .responseFields()
                                        .requestSchema(
                                                Schema.schema("창고 생성 Request")
                                        ).build()
                        )));
    }

    @Test
    @DisplayName("storageList: 요청 회원의 창고 목록 조회")
    void storageList() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("http://localhost:8080/api/storage/")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(commonDtoHandlerMock, only()).userIdFromAuthentication(any());
        verify(storageServiceMock, only()).storageList(anyLong());
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
                                                // 아래 응답 객체 작성 방법 공부 하여 수정하기 fix
                                                fieldWithPath("storageList").description("StorageInfo 객체 목록"),
//                                                fieldWithPath("storageList.id").description("창고 id"),
//                                                fieldWithPath("storageList.name").description("창고 이름"),
//                                                fieldWithPath("storageList.imgUri").description("창고 이미지 링크"),
//                                                fieldWithPath("storageList.address").description("창고 주소"),
                                                fieldWithPath("count").description("보유 창고 개수")
                                        )
                                        .responseSchema(
                                                Schema.schema("창고 목록 조회 Request")
                                        )
                                        .build()
                        )
                )
        );

    }
    
    @Test
    @DisplayName("storageNearbyList: 근처 창고 조회")
    void storageNearbyTest() throws Exception {
        // given
        String centerLongitude ="126.0";
        String centerLatitude = "37.0";
        double centerLon = Double.parseDouble(centerLongitude);
        double centerLat = Double.parseDouble(centerLatitude);
        Point center = geomUtil.createPoint(centerLon, centerLat);


        String longitude ="126.01";
        String latitude = "37.01";
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        Point testLocation = geomUtil.createPoint(lon, lat);


        Storage testStorage = Storage.builder().id(1L).name("test").location(testLocation).address("address").build();
        List<Storage> storageNearbyList = new ArrayList<>(List.of(testStorage));

        BDDMockito.given(storageServiceMock.storageNearby(any(Point.class))).willReturn(storageNearbyList);
        BDDMockito.given(geomUtilMock.createPoint(anyDouble(), anyDouble())).willReturn(center);
        BDDMockito.given(geomUtilMock.calculateDistance(any(Point.class), any(Point.class))).willReturn(0);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("http://localhost:8080/api/storage/nearby")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .queryParam("longitude", centerLongitude)
                        .queryParam("latitude", centerLatitude)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(geomUtilMock, times(1)).createPoint(126.0, 37.0);
        verify(storageServiceMock, only()).storageNearby(geomUtil.createPoint(126.0, 37.0));
        verify(geomUtilMock, atLeast(1)).calculateDistance(any(Point.class), any(Point.class));

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
                                                parameterWithName("longitude").description("경도"),
                                                parameterWithName("latitude").description("위도"),
                                                parameterWithName("_csrf").ignored()
                                        )
                                        .responseFields(
                                                fieldWithPath("[]").description("창고 목록"),
                                                fieldWithPath("[].id").description("창고 id"),
                                                fieldWithPath("[].name").description("창고 이름"),
                                                fieldWithPath("[].longitude").description("경도"),
                                                fieldWithPath("[].latitude").description("위도"),
                                                fieldWithPath("[].distance").description("좌표와의 거리")
                                        )
                                        .requestSchema(
                                                Schema.schema("근처 창고 조회 Request")
                                        )
                                        .responseSchema(
                                                Schema.schema("근처 창고 조회 결과")
                                        ).build()
                        )));
    }

    @Test
    @DisplayName("storageInfo: 창고 상세 정보 조회")
    void storageInfoTest() throws Exception {
        // given
        Storage testStorage = Storage.builder()
                .name("name").description("description")
                .address("address").imgUrl("imgUrl")
                .location(geomUtil.createPoint(126.0, 37.0)).build();
        StorageInfoResponseDto responseDto = new StorageInfoResponseDto(testStorage);

        Long testStorageId = 123L;

        BDDMockito.given(storageServiceMock.StorageInfo(testStorageId)).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("http://localhost:8080/api/storage/{storageId}", testStorageId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        verify(storageServiceMock, times(1)).StorageInfo(123L);

        // RestDocs
        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("창고")
                                .description("창고 상세 정보 조회")
                                .pathParameters(
                                        parameterWithName("storageId").description("창고 id")
                                )
                                .responseFields(
                                        fieldWithPath("name").description("창고 이름"),
                                        fieldWithPath("description").description("창고 소개"),
                                        fieldWithPath("imgUrl").description("창고 이미지 url"),
                                        fieldWithPath("longitude").description("창고 좌표 - 경도"),
                                        fieldWithPath("latitude").description("창고 좌표 - 위도"),
                                        fieldWithPath("address").description("창고 위치의 주소")
                                ).build()
                )));
    }
}