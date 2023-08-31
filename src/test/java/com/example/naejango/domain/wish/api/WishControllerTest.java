package com.example.naejango.domain.wish.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.wish.application.WishService;
import com.example.naejango.domain.wish.dto.response.FindWishResponseDto;
import com.example.naejango.global.common.handler.AuthenticationHandler;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(WishController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class WishControllerTest extends RestDocsSupportTest {
    @MockBean
    WishService wishService;
    @MockBean
    AuthenticationHandler authenticationHandler;

    @Nested
    @Order(1)
    @DisplayName("Controller Wish 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findWish {
        Long userId;
        List<FindWishResponseDto> findWishResponseDtoList =
                new ArrayList<>(List.of(
                        new FindWishResponseDto(1L, "아이템1 이름", "아이템1 설명", "이미지1 Url", ItemType.BUY, "카테고리"),
                        new FindWishResponseDto(2L, "아이템2 이름", "아이템2 설명", "이미지2 Url", ItemType.BUY, "카테고리")
                ));

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("관심_목록_조회_성공")
        void 관심_목록_조회_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(wishService.findWish(userId))
                    .willReturn(findWishResponseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/wish")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("관심")
                                    .description("관심 목록 조회")
                                    .responseFields(
                                            fieldWithPath("[].id").description("아이템 ID"),
                                            fieldWithPath("[].name").description("아이템 이름"),
                                            fieldWithPath("[].description").description("아이템 설명"),
                                            fieldWithPath("[].imgUrl").description("이미지 URL"),
                                            fieldWithPath("[].type").description("아이템 타입"),
                                            fieldWithPath("[].category").description("카테고리")
                                    )
                                    .responseSchema(Schema.schema("관심 목록 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Controller Wish 등록")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class addWish {
        Long itemId=1L;
        Long userId;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("관심_등록_성공")
        void 관심_등록_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/wish/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("관심")
                                    .description("관심 등록")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("관심 등록 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Controller Wish 해제")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class deleteWish {
        Long itemId=1L;
        Long userId;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("관심_해제_성공")
        void 관심_해제_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/wish/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("관심")
                                    .description("관심 해제")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("관심 해제 Response"))
                                    .build()
                    )));
        }
    }
}