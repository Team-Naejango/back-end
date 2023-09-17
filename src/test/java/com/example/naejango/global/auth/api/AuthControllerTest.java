package com.example.naejango.global.auth.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.AccessTokenReissuer;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends RestDocsSupportTest {
    @MockBean UserService userServiceMock;
    @MockBean RefreshTokenRepository refreshTokenRepositoryMock;
    @MockBean AuthenticationHandler authenticationHandlerMock;
    @MockBean JwtCookieHandler jwtCookieHandlerMock;
    @MockBean JwtValidator jwtValidatorMock;
    @MockBean AccessTokenReissuer accessTokenReissuerMock;

    @Nested
    @DisplayName("로그아웃")
    class logout {
        @Test
        @Tag("api")
        @DisplayName("성공")
        void test1 () throws Exception {
            // given
            BDDMockito.given(authenticationHandlerMock.getUserId(any())).willReturn(1L);
            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/auth/logout"));

            // then
            resultActions.andExpect(MockMvcResultMatchers
                    .status().isOk()
            );

            // RestDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("시큐리티")
                            .summary("로그아웃")
                            .responseFields(
                                    fieldWithPath("message").description("결과 메세지"),
                                    fieldWithPath("result").description("유저 아이디")
                            )
                            .build()
            )));
        }
    }

    @Nested
    @DisplayName("엑세스 토큰 재발급")
    class refreshAccessToken {
        @Test
        @Tag("api")
        @DisplayName("성공")
        void test1 () throws Exception {
            // given
            BDDMockito.given(accessTokenReissuerMock.reissueAccessToken(any())).willReturn(Optional.of("재발급 된 토큰"));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/auth/refresh"));

            // then
            resultActions.andExpect(MockMvcResultMatchers
                    .status().isOk()
            );

            // RestDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("시큐리티")
                            .summary("엑세스 토큰 재발급")
                            .responseFields(
                                    fieldWithPath("message").description("결과 메세지"),
                                    fieldWithPath("result").description("재발급 된 엑세스 토큰")
                            )
                            .build()
                    )));
        }
    }

}
