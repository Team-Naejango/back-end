package com.example.naejango.domain.user.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.CreateUserProfileCommandDto;
import com.example.naejango.domain.user.dto.ModifyUserProfileCommandDto;
import com.example.naejango.domain.user.dto.UserProfileDto;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@Slf4j
class UserControllerTest extends RestDocsSupportTest {

    @MockBean
    JwtValidator jwtValidatorMock;
    @MockBean
    JwtCookieHandler jwtCookieHandler;
    @MockBean
    UserService userServiceMock;
    @MockBean
    AccountService accountServiceMock;
    @MockBean
    AuthenticationHandler authenticationHandlerMock;

    @Nested
    @DisplayName("유저 프로필 생성")
    class createUserProfileTest {
        CreateUserProfileRequestDto requestDto = CreateUserProfileRequestDto
                .builder()
                .birth("19910617")
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("01094862225")
                .intro("소개글")
                .gender(Gender.MALE)
                .build();

        User user = User.builder().id(1L).role(Role.TEMPORAL).build();

        @Test
        @Tag("api")
        @DisplayName("생성 성공")
        void test1() throws Exception {
            // given
            String requestJson = objectMapper.writeValueAsString(requestDto);
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .post("/api/user/profile")
                            .header("Authorization", "access 토큰")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            verify(userServiceMock, times(1)).createUserProfile(new CreateUserProfileCommandDto(user.getId(), requestDto));
            verify(accountServiceMock, times(1)).createAccount(user.getId());
            resultActions.andExpect(
                    status().isOk());

            resultActions.andDo(restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("회원")
                                            .summary("프로필 생성")
                                            .description("유저프로필 생성 하여 요청한 유저 엔티티에 할당, 계좌 생성")
                                            .requestFields(
                                                    fieldWithPath("birth").description("생년월일"),
                                                    fieldWithPath("nickname").description("닉네임"),
                                                    fieldWithPath("imgUrl").description("이미지 링크"),
                                                    fieldWithPath("phoneNumber").description("전화번호"),
                                                    fieldWithPath("intro").description("소개글"),
                                                    fieldWithPath("gender").description("성별(남/여)")
                                            )
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result").description("null")
                                            )
                                            .requestSchema(
                                                    Schema.schema("유저 프로필 생성 Request")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("내 프로필 조회")
    class MyProfileTest {
        UserProfile testUserProfile = UserProfile.builder()
                .birth("생년월")
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("전화번호")
                .intro("소개글")
                .gender(Gender.MALE)
                .build();

        User user = User.builder()
                .id(1L)
                .userProfile(testUserProfile)
                .build();

        Account account = Account.builder()
                .balance(1000)
                .user(user)
                .build();



        @Test
        @Tag("api")
        @DisplayName("조회 성공")
        void test1() throws Exception {
            //given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(1L);
            BDDMockito.given(userServiceMock.findOtherUserProfile(user.getId())).willReturn(new UserProfileDto(testUserProfile));
            BDDMockito.given(accountServiceMock.getAccount(user.getId())).willReturn(account.getBalance());

            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/user/profile")
                            .header("Authorization", "access token")
            );

            verify(userServiceMock, times(1)).findOtherUserProfile(user.getId());
            verify(accountServiceMock, times(1)).getAccount(user.getId());

            resultActions.andDo(
                    restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("회원")
                                            .description("요청하는 유저의 프로필 조회")
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result").description("조회 결과"),
                                                    fieldWithPath("result.userId").description("유저 ID"),
                                                    fieldWithPath("result.nickname").description("닉네임"),
                                                    fieldWithPath("result.intro").description("소개글"),
                                                    fieldWithPath("result.imgUrl").description("이미지 링크"),
                                                    fieldWithPath("result.gender").description("성별(남/여)"),
                                                    fieldWithPath("result.birth").description("생년 월일"),
                                                    fieldWithPath("result.phoneNumber").description("전화번호"),
                                                    fieldWithPath("result.balance").description("잔고")
                                            )
                                            .requestSchema(
                                                    Schema.schema("내 프로필 조회 Response")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("다른 유저 프로필 조회")
    class findUserProfile {
        UserProfile testUserProfile = UserProfile.builder()
                .birth("19910617")
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("전화번호")
                .intro("소개글")
                .gender(Gender.MALE)
                .lastLogin(LocalDateTime.now().minusMinutes(15))
                .build();

        User user = User.builder()
                .id(1L)
                .userProfile(testUserProfile).build();

        @Test
        @Tag("api")
        @DisplayName("조회 성공")
        void test1() throws Exception {
            //given
            BDDMockito.given(userServiceMock.findOtherUserProfile(user.getId()))
                    .willReturn(new UserProfileDto(testUserProfile));

            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .get("/api/user/profile/{userId}", user.getId())
                            .header("Authorization", "access token")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            verify(userServiceMock, times(1)).findOtherUserProfile(user.getId());

            resultActions.andDo(
                    restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("회원")
                                            .description("다른 유저의 프로필 조회")
                                            .responseFields(
                                                    fieldWithPath("message").description("결과 메세지"),
                                                    fieldWithPath("result").description("조회 결과"),
                                                    fieldWithPath("result.nickname").description("닉네임"),
                                                    fieldWithPath("result.imgUrl").description("이미지 링크"),
                                                    fieldWithPath("result.age").description("연령 (10대/20대...)"),
                                                    fieldWithPath("result.gender").description("성별(남/여)"),
                                                    fieldWithPath("result.intro").description("소개글"),
                                                    fieldWithPath("result.lastLogin").description("마지막 로그인")
                                            )
                                            .requestSchema(
                                                    Schema.schema("다른 유저의 프로필 조회 Response")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("유저 프로필 수정")
    class modifyUserProfile {

        UserProfile testUserProfile = UserProfile.builder()
                .birth("19910617")
                .nickname("닉네임")
                .imgUrl("이미지 링크")
                .phoneNumber("01012345678")
                .intro("소개글")
                .gender(Gender.MALE)
                .build();

        User user = User.builder()
                .id(1L)
                .userProfile(testUserProfile)
                .build();

        @Test
        @Tag("api")
        @DisplayName("수정 성공")
        void test1() throws Exception {
            //given
            var requestDto = ModifyUserProfileRequestDto.builder()
                    .nickname("변경 닉네임").intro("변경 소개글").imgUrl("변경 이미지").build();

            var commandDto = new ModifyUserProfileCommandDto(user.getId(), requestDto);
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(user.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    RestDocumentationRequestBuilders
                            .patch("/api/user/profile")
                            .header("Authorization", "access token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
            );

            // then
            verify(userServiceMock, atLeastOnce()).modifyUserProfile(commandDto);
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("message").value("수정 완료"));

            // restDocs
            resultActions.andDo(
                    restDocs.document(
                            resource(
                                    ResourceSnippetParameters.builder()
                                            .tag("회원")
                                            .summary("회원 프로필 수정")
                                            .description("요청된 Dto 에 따라 회원의 프로필을 변경합니다.")
                                            .requestFields(
                                                    fieldWithPath("nickname").description("닉네임"),
                                                    fieldWithPath("intro").description("소개"),
                                                    fieldWithPath("imgUrl").description("이미지 링크")
                                            )
                                            .responseFields(
                                                    fieldWithPath("message").description("수정 결과 메세지"),
                                                    fieldWithPath("result").description("")
                                            )
                                            .requestSchema(
                                                    Schema.schema("ModifyUserProfileResponseDto.Get")
                                            )
                                            .build()
                            )
                    )
            );
        }
    }

}