package com.example.naejango.domain.follow.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.follow.application.FollowService;
import com.example.naejango.domain.follow.dto.response.FindFollowResponseDto;
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
        List<FindFollowResponseDto> findFollowResponseDtoList =
                new ArrayList<>(List.of(
                        new FindFollowResponseDto(1L, "창고1 이름", "창고1 설명", "창고1 Url"),
                        new FindFollowResponseDto(2L, "창고2 이름", "창고2 설명", "창고2 Url")
                ));

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("팔로우_목록_조회_성공")
        void 팔로우_목록_조회_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(followService.findFollow(userId))
                    .willReturn(findFollowResponseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/follow")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("팔로우")
                                    .description("팔로우 목록 조회")
                                    .responseFields(
                                            fieldWithPath("[].id").description("창고 ID"),
                                            fieldWithPath("[].name").description("창고 이름"),
                                            fieldWithPath("[].description").description("창고 설명"),
                                            fieldWithPath("[].imgUrl").description("이미지 URL")
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
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/follow/{storageId}", storageId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
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
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
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
            BDDMockito.given(authenticationHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/follow/{storageId}", storageId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
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
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("팔로우 해제 Response"))
                                    .build()
                    )));
        }
    }
}