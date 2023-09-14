package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.CreateItemCommandDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemCommandDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(ItemController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ItemControllerTest extends RestDocsSupportTest {

    @MockBean
    ItemService itemService;
    @MockBean
    AuthenticationHandler authenticationHandler;
    @MockBean
    CategoryRepository categoryRepository;
    @MockBean
    ChannelRepository channelRepositoryMock;
    @MockBean
    GeomUtil geomUtilMock;

    @Nested
    @Order(1)
    @DisplayName("Controller 아이템 생성")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class createItem {
        Long userId;

        CreateItemRequestDto createItemRequestDto =
                CreateItemRequestDto.builder()
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .storageId(1L)
                        .build();

        CreateItemResponseDto createItemResponseDto =
                CreateItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_생성_성공")
        void 아이템_생성_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemCommandDto.class)))
                    .willReturn(createItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/item")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("result.id").isNumber());
            resultActions.andExpect(MockMvcResultMatchers.header().exists("Location"));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 생성")
                                    .responseHeaders(
                                            headerWithName("Location").description("생성된 아이템 URI")
                                    )
                                    .requestFields(
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("category").description("카테고리"),
                                            fieldWithPath("storageId").description("창고 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 ID"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .requestSchema(Schema.schema("아이템 생성 Request"))
                                    .responseSchema(Schema.schema("아이템 생성 Response"))
                                    .build()
                    )));
        }

        @Test
        @Order(2)
        @DisplayName("실패_잘못된_카테고리_이름으로_요청_404_발생")
        void 실패_잘못된_카테고리_이름으로_요청_404_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemCommandDto.class)))
                    .willThrow(new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/item")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());

        }


        @Test
        @Order(3)
        @DisplayName("실패_등록되지_않은_창고_ID_값으로_요청_404_발생")
        void 실패_등록되지_않은_창고_ID_값으로_요청_404_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemCommandDto.class)))
                    .willThrow(new CustomException(ErrorCode.STORAGE_NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/item")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Controller 아이템 정보 조회")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findItem {
        Long itemId=1L;
        FindItemResponseDto findItemResponseDto =
                FindItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_정보_조회_성공")
        void 아이템_정보_조회_성공() throws Exception {
            // given
            BDDMockito.given(itemService.findItem(any()))
                    .willReturn(findItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 정보 조회")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("아이템 정보 조회 Response"))
                                    .build()
                    )));
        }
    }

//    @Nested
//    @Tag("api")
//    @DisplayName("창고 검색")
//    class SearchStorageByConditions {
//
//        @Test
//        @DisplayName("모든 조건으로 창고 검색")
//        void test1() throws Exception {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            Category cat1 = new Category(1, "의류");
//            int rad = 1000;
//            int page = 0;
//            int size = 10;
//            Storage testStorage1 = Storage.builder().name("테스트1").location(geomUtil.createPoint(127.021, 37.491)).address("").build();
//            Storage testStorage2 = Storage.builder().name("테스트2").location(geomUtil.createPoint(127.022, 37.492)).address("").build();
//            StorageAndDistanceDto result1 = new StorageAndDistanceDto(testStorage1, 100);
//            StorageAndDistanceDto result2 = new StorageAndDistanceDto(testStorage2, 200);
//
//
//            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{"%유니클로%", "%청바지%"}, ItemType.INDIVIDUAL_BUY, true);
//            BDDMockito.given(geomUtilMock.createPoint(127.02, 37.49)).willReturn(center);
//            BDDMockito.given(categoryRepositoryMock.findById(1)).willReturn(Optional.of(cat1));
//            BDDMockito.given(storageRepositoryMock.searchItemsByConditions(center, rad, page, size, conditions))
//                    .willReturn(Arrays.asList(result1, result2));
//
//            // when
//            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
//                    .get("/api/storage/search")
//                    .queryParam("lon", "127.02")
//                    .queryParam("lat", "37.49")
//                    .queryParam("rad","1000")
//                    .queryParam("page", "0")
//                    .queryParam("size", "10")
//                    .queryParam("cat", "1")
//                    .queryParam("keyword", "유니클로 청바지")
//                    .queryParam("type", "INDIVIDUAL_BUY")
//                    .queryParam("status", "true")
//                    .characterEncoding(StandardCharsets.UTF_8)
//                    .header("Authorization", "엑세스 토큰")
//                    .with(SecurityMockMvcRequestPostProcessors.csrf())
//            );
//
//            // then
//            verify(geomUtilMock, times(1)).createPoint(127.02, 37.49);
//            verify(categoryRepositoryMock, times(1)).findById(1);
//            verify(storageRepositoryMock, times(1)).searchItemsByConditions(center, rad, page, size, conditions);
//
//            // restDocs
//            resultActions.andDo(restDocs.document(
//                    resource(ResourceSnippetParameters.builder()
//                            .tag("창고")
//                            .summary("창고 검색")
//                            .description("조건에 맞는 창고를 검색합니다.\n\n" +
//                                    "좌표, 반경, 카테고리, 키워드, 타입, 상태를 조건으로 받습니다.\n\n" +
//                                    "창고 정보만 응답합니다. 추후 아이템 정보도 가지고 올 수 있도록 수정하겠습니다.")
//                            .requestParameters(
//                                    parameterWithName("lon").description("중심 경도 좌표"),
//                                    parameterWithName("lat").description("중심 위도 좌표"),
//                                    parameterWithName("rad").description("반경 (1,000~5,000m)"),
//                                    parameterWithName("page").description("페이지"),
//                                    parameterWithName("size").description("사이즈"),
//                                    parameterWithName("cat").description("카테고리 ID"),
//                                    parameterWithName("keyword").description("검색 키워드(2~10자)"),
//                                    parameterWithName("type").description("BUY/SELL"),
//                                    parameterWithName("status").description("상태 (true/false)"),
//                                    parameterWithName("_csrf").ignored()
//                            ).responseFields(
//                                    fieldWithPath("message").description("조회 결과 메세지"),
//                                    fieldWithPath("coord").description("중심 좌표"),
//                                    fieldWithPath("coord.longitude").description("중심 경도 좌표"),
//                                    fieldWithPath("coord.latitude").description("중심 위도 좌표"),
//                                    fieldWithPath("radius").description("반경"),
//                                    fieldWithPath("page").description("페이지"),
//                                    fieldWithPath("size").description("조회 결과물 수"),
//                                    fieldWithPath("searchingConditions").description("검색 조건"),
//                                    fieldWithPath("searchingConditions.cat").description("검색 조건 : 카테고리"),
//                                    fieldWithPath("searchingConditions.cat.id").description("카테고리 아이디"),
//                                    fieldWithPath("searchingConditions.cat.name").description("카테고리 이름"),
//                                    fieldWithPath("searchingConditions.keyword[]").description("검색 조건 : 키워드"),
//                                    fieldWithPath("searchingConditions.itemType").description("검색 조건 : 상품 타입"),
//                                    fieldWithPath("searchingConditions.status").description("검색 조건 : 상품 상태"),
//                                    fieldWithPath("searchResult[].storageId").description("창고 Id"),
//                                    fieldWithPath("searchResult[].name").description("창고 이름"),
//                                    fieldWithPath("searchResult[].imgUrl").description("이미지 링크"),
//                                    fieldWithPath("searchResult[].coord").description("창고 좌표"),
//                                    fieldWithPath("searchResult[].address").description("창고 주소"),
//                                    fieldWithPath("searchResult[].distance").description("거리"),
//                                    fieldWithPath("searchResult[].description").description("창고 설명")
//                            ).requestSchema(
//                                    Schema.schema("창고 검색 Request")
//                            ).responseSchema(
//                                    Schema.schema("창고 검색 Response")
//                            )
//                            .build())
//            ));
//        }
//    }

    @Nested
    @Order(3)
    @DisplayName("Controller 아이템 정보 수정")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class modifyItem {
        Long userId;
        Long itemId=1L;
        ModifyItemRequestDto modifyItemRequestDto =
                ModifyItemRequestDto.builder()
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .build();

        ModifyItemResponseDto modifyItemResponseDto =
                ModifyItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_정보_수정_성공")
        void 아이템_정보_수정_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(modifyItemRequestDto);

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.modifyItem(any(), any(), any(ModifyItemCommandDto.class)))
                    .willReturn(modifyItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/item/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 정보 수정")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .requestFields(
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("category").description("카테고리")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .requestSchema(Schema.schema("아이템 정보 수정 Request"))
                                    .responseSchema(Schema.schema("아이템 정보 수정 Response"))
                                    .build()
                    )));
        }
    }

}