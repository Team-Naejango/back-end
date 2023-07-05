package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(ItemController.class)
class ItemControllerTest extends RestDocsSupportTest {

    @MockBean
    ItemService itemService;

    @MockBean
    UserService userService;

    @Nested
    @DisplayName("Controller 아이템 생성")
    @WithMockUser()
    class createItem {
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
                CreateItemResponseDto.builder().build();



        @Test
        @Order(1)
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

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

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 생성")
                                    .requestHeaders(
                                            headerWithName("Authorization").description("JWT")
                                    )
                                    .requestFields(
                                            fieldWithPath("name").description("이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("type").description("아이템 타입"),
                                            fieldWithPath("category").description("카테고리"),
                                            fieldWithPath("storageIdList").description("창고 ID 리스트")
                                    )
                                    .responseFields(

                                    )
                                    .requestSchema(Schema.schema("CreateItemRequestDto.Post"))
                                    .responseSchema(Schema.schema("CreateItemResponseDto.Post"))
                                    .build()
                    )));
        }

        @Test
        @Order(2)
        @DisplayName("실패_잘못된_카테고리_이름으로_요청_404_발생")
        void 실패_잘못된_카테고리_이름으로_요청_404_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

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
        @Order(3)
        @DisplayName("실패_등록되지_않은_창고_ID_값으로_요청_404_발생")
        void 실패_등록되지_않은_창고_ID_값으로_요청_404_발생() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

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
}