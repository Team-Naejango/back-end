package com.example.naejango.domain.user.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(UserController.class)
@Slf4j
class UserControllerTest extends RestDocsSupportTest {
    @MockBean
    UserService userServiceMock;
    @MockBean
    CommonDtoHandler commonDtoHandlerMock;

    @Test
    @DisplayName("creatUserProfile : UserProfile 을 생성하고 User 에 할당")
    void createUserProfileTest() throws Exception {
        //given
        CreateUserProfileRequestDto requestDto = CreateUserProfileRequestDto
                .builder()
                .age(20)
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("전화번호")
                .intro("소개글")
                .gender(Gender.Male)
                .build();

        String requestJson = objectMapper.writeValueAsString(requestDto);

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .post("http://localhost:8080/api/user/profile")
                        .header("Authorization", "access 토큰")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).createUserProfile(any(CreateUserProfileRequestDto.class), anyLong());

        resultActions.andExpect(
                MockMvcResultMatchers
                        .status().isOk());

        resultActions.andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("유저프로필 생성")
                                        .description("유저프로필 생성 하여 요청한 유저 엔티티에 할당")
                                        .requestHeaders(
                                                HeaderDocumentation.headerWithName("Authorization").description("access 토큰")
                                        )
                                        .requestFields(
                                                fieldWithPath("age").description("나이"),
                                                fieldWithPath("nickname").description("닉네임"),
                                                fieldWithPath("imgUrl").description("이미지 링크"),
                                                fieldWithPath("phoneNumber").description("전화번호"),
                                                fieldWithPath("intro").description("소개글"),
                                                fieldWithPath("gender").description("성별(male, female)")
                                        )
                                        .responseFields()
                                        .requestSchema(
                                                Schema.schema("CreateUserProfileRequestDto.Post")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("userProfile : UserProfile 조회")
    void userProfileTest() throws Exception {
        //given
        UserProfile testUserProfile = UserProfile.builder()
                .age(20)
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("전화번호")
                .intro("소개글")
                .gender(Gender.Male)
                .build();

        BDDMockito.given(userServiceMock.findUser(any(Long.class)))
                .willReturn(User.builder().userProfile(testUserProfile).build());

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get("http://localhost:8080/api/user/profile")
                        .header("Authorization", "access token")
        );

        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).findUser(anyLong());

        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk()
                ).andExpect(jsonPath("$.age").value(testUserProfile.getAge())
                ).andExpect(jsonPath("$.nickname").value(testUserProfile.getNickname())
                ).andExpect(jsonPath("$.imgUrl").value(testUserProfile.getImgUrl())
                ).andExpect(jsonPath("$.gender").value(testUserProfile.getGender().name())
                ).andExpect(jsonPath("$.intro").value(testUserProfile.getIntro()));

        resultActions.andDo(
                restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("UserProfile 조회")
                                        .description("요청하는 유저의 프로필 조회")
                                        .requestHeaders(
                                                HeaderDocumentation.headerWithName("Authorization").description("access token")
                                        )
                                        .requestFields()
                                        .responseFields(
                                                fieldWithPath("age").description("나이"),
                                                fieldWithPath("nickname").description("닉네임"),
                                                fieldWithPath("imgUrl").description("이미지 링크"),
                                                fieldWithPath("phoneNumber").description("전화번호"),
                                                fieldWithPath("intro").description("소개글"),
                                                fieldWithPath("gender").description("성별(male, female)")
                                        )
                                        .requestSchema(
                                                Schema.schema("UserProfileResponseDto.Get")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("modifyProfile : UserProfile 수정")
    void modifyProfileTest() throws Exception {
        //given
        UserProfile testUserProfile = UserProfile.builder()
                .age(20)
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("전화번호")
                .intro("소개글")
                .gender(Gender.Male)
                .build();

        ModifyUserProfileRequestDto requestDto = ModifyUserProfileRequestDto.builder().build();

        String requestJson = objectMapper.writeValueAsString(requestDto);

        BDDMockito.given(userServiceMock.findUser(any(Long.class)))
                .willReturn(User.builder().userProfile(testUserProfile).build());

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .patch("http://localhost:8080/api/user/profile")
                        .header("Authorization", "access token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );

        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).modifyUserProfile(any(ModifyUserProfileRequestDto.class), anyLong());

        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk()
                ).andExpect(jsonPath("$.nickname").value(testUserProfile.getNickname())
                ).andExpect(jsonPath("$.imgUrl").value(testUserProfile.getImgUrl())
                ).andExpect(jsonPath("$.age").value(testUserProfile.getAge())
                ).andExpect(jsonPath("$.gender").value(testUserProfile.getGender().name())
                ).andExpect(jsonPath("$.intro").value(testUserProfile.getIntro()));

        resultActions.andDo(
                restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("UserProfile 수정")
                                        .description("요청된 Dto 에 따라 회원의 프로필 변경, 수정된 프로필 반환")
                                        .requestHeaders(
                                                HeaderDocumentation.headerWithName("Authorization").description("access 토큰")
                                        )
                                        .requestFields()
                                        .responseFields(
                                                fieldWithPath("age").description("나이"),
                                                fieldWithPath("nickname").description("닉네임"),
                                                fieldWithPath("imgUrl").description("이미지 링크"),
                                                fieldWithPath("phoneNumber").description("전화번호"),
                                                fieldWithPath("intro").description("소개글"),
                                                fieldWithPath("gender").description("성별(male, female)")
                                        )
                                        .requestSchema(
                                                Schema.schema("UserProfileResponseDto.Get")
                                        )
                                        .build()
                        )
                )
        );
    }


    @Test
    @DisplayName("deleteUser: User 및 해당 User 의 UserProfile 삭제")
    void deleteUserTest() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .delete("http://localhost:8080/api/user/")
                        .header("Authorization", "access 토큰")
                        .cookie(new Cookie("Refresh_Token_Cookie", "Refresh Token"))
        );

        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).deleteUser(any(), any());

        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

        resultActions.andDo(
                restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("User, UserProfile 삭제")
                                        .description("요청 유저의 User, UserProfile 삭제")
                                        .requestHeaders(
                                                HeaderDocumentation.headerWithName("Authorization").description("access 토큰")
                                        )
                                        .requestFields()
                                        .responseFields()
                                        .build()
                        )
                )
        );
        // then
    }




}