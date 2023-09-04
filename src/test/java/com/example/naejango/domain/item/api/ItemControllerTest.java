package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.ConnectItemRequestDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
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

import java.util.ArrayList;
import java.util.List;

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
    private GeomUtil geomUtilMock;

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
                        .type(ItemType.SELL)
                        .category("카테고리")
                        .storageIdList(new ArrayList<>(List.of(1L, 2L)))
                        .build();

        CreateItemResponseDto createItemResponseDto =
                CreateItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .type(ItemType.SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_생성_성공")
        void 아이템_생성_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemRequestDto.class)))
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
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("id").isNumber());
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
                                            fieldWithPath("type").description("아이템 타입 (BUY or SELL)"),
                                            fieldWithPath("category").description("카테고리"),
                                            fieldWithPath("storageIdList").description("창고 ID 리스트")
                                    )
                                    .responseFields(
                                            fieldWithPath("id").description("아이템 ID"),
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("type").description("아이템 타입 (BUY or SELL)"),
                                            fieldWithPath("category").description("카테고리")
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

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemRequestDto.class)))
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
        @DisplayName("실패_창고_생성_전에_아이템_등록_요청_400_발생")
        void 실패_창고_생성_전에_아이템_등록_요청_400_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemRequestDto.class)))
                    .willThrow(new CustomException(ErrorCode.STORAGE_NOT_EXIST));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/item")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("실패_등록되지_않은_창고_ID_값으로_요청_404_발생")
        void 실패_등록되지_않은_창고_ID_값으로_요청_404_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemRequestDto.class)))
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
                        .type(ItemType.SELL)
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
                                            fieldWithPath("id").description("아이템 id"),
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("type").description("아이템 타입 (BUY or SELL)"),
                                            fieldWithPath("category").description("카테고리")
                                    )
                                    .responseSchema(Schema.schema("아이템 정보 조회 Response"))
                                    .build()
                    )));
        }
    }

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
                        .type(ItemType.SELL)
                        .category("카테고리")
                        .build();

        ModifyItemResponseDto modifyItemResponseDto =
                ModifyItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .type(ItemType.SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_정보_수정_성공")
        void 아이템_정보_수정_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(modifyItemRequestDto);

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.modifyItem(any(), any(), any(ModifyItemRequestDto.class)))
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
                                            fieldWithPath("type").description("아이템 타입 (BUY or SELL)"),
                                            fieldWithPath("category").description("카테고리")
                                    )
                                    .responseFields(
                                            fieldWithPath("id").description("아이템 id"),
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("type").description("아이템 타입 (BUY or SELL)"),
                                            fieldWithPath("category").description("카테고리")
                                    )
                                    .requestSchema(Schema.schema("아이템 정보 수정 Request"))
                                    .responseSchema(Schema.schema("아이템 정보 수정 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Controller 아이템 창고 등록 수정")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class connectItem {
        Long userId;
        Long itemId=1L;
        ConnectItemRequestDto connectItemRequestDto =
                ConnectItemRequestDto.builder()
                        .storageIdList(new ArrayList<>(List.of(1L, 2L)))
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_창고_등록_수정_성공")
        void 아이템_창고_등록_수정_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(connectItemRequestDto);

            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/item/connect/{itemId}", itemId)
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
                                    .description("아이템 창고 등록 수정")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .requestFields(
                                            fieldWithPath("storageIdList").description("창고 ID 리스트")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .requestSchema(Schema.schema("아이템 창고 등록 수정 Request"))
                                    .responseSchema(Schema.schema("아이템 창고 등록 수정 Response"))
                                    .build()
                    )));
        }
    }

}