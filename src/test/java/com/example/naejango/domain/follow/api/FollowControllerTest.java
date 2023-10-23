package com.example.naejango.domain.follow.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.follow.application.FollowService;
import com.example.naejango.domain.follow.dto.FollowStorageItemsDto;
import com.example.naejango.domain.follow.dto.response.FindFollowResponseDto;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(FollowController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class FollowControllerTest extends RestDocsSupportTest {
    @MockBean
    FollowService followService;

    @MockBean
    AuthenticationHandler authenticationHandler;

    @Nested
    @Order(1)
    @DisplayName("Controller Follow 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findFollow {
        Long userId;
        List<FollowStorageItemsDto> list1 =
                new ArrayList<>(List.of(
                        new FollowStorageItemsDto(1L, "아이템 이름1", "아이템 설명", "이미지 URL", ItemType.INDIVIDUAL_BUY, true),
                        new FollowStorageItemsDto(2L, "아이템 이름2", "아이템 설명", "이미지 URL", ItemType.INDIVIDUAL_SELL, false)
                ));
        List<FollowStorageItemsDto> list2 =
                new ArrayList<>(List.of(
                        new FollowStorageItemsDto(3L, "아이템 이름3", "아이템 설명", "이미지 URL", ItemType.INDIVIDUAL_BUY, true),
                        new FollowStorageItemsDto(4L, "아이템 이름4", "아이템 설명", "이미지 URL", ItemType.INDIVIDUAL_SELL, false)
                ));
        List<FindFollowResponseDto> findFollowResponseDtoList =
                new ArrayList<>(List.of(
                        new FindFollowResponseDto(1L, "창고1 이름", "창고1 설명", "창고1 Url", list1),
                        new FindFollowResponseDto(2L, "창고2 이름", "창고2 설명", "창고2 Url", list2)
                ));

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("팔로우_목록_조회_성공")
        void 팔로우_목록_조회_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(followService.findFollow(userId))
                    .willReturn(findFollowResponseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/follow")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("팔로우")
                                    .description("팔로우 목록 조회")
                                    .responseFields(
                                            fieldWithPath("result[].id").description("창고 ID"),
                                            fieldWithPath("result[].name").description("창고 이름"),
                                            fieldWithPath("result[].description").description("창고 설명"),
                                            fieldWithPath("result[].imgUrl").description("창고 이미지 URL"),
                                            fieldWithPath("result[].items").description("창고 아이템 목록"),
                                            fieldWithPath("result[].items[].itemId").description("아이템 ID"),
                                            fieldWithPath("result[].items[].name").description("아이템 이름"),
                                            fieldWithPath("result[].items[].description").description("아이템 설명"),
                                            fieldWithPath("result[].items[].imgUrl").description("아이템 이미지 URL"),
                                            fieldWithPath("result[].items[].itemType").description("아이템 타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)"),
                                            fieldWithPath("result[].items[].status").description("아이템 상태 (true 거래중/false 거래완료)"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("팔로우 목록 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Controller Follow 등록")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class addFollow {
        Long storageId=1L;
        Long userId;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("팔로우_등록_성공")
        void 팔로우_등록_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/follow/{storageId}", storageId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("팔로우")
                                    .description("팔로우 등록")
                                    .pathParameters(
                                            parameterWithName("storageId").description("창고 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("팔로우 등록 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Controller Follow 해제")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class deleteFollow {
        Long storageId=1L;
        Long userId;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("관심_해제_성공")
        void 관심_해제_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/follow/{storageId}", storageId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("팔로우")
                                    .description("팔로우 해제")
                                    .pathParameters(
                                            parameterWithName("storageId").description("창고 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("팔로우 해제 Response"))
                                    .build()
                    )));
        }
    }
}