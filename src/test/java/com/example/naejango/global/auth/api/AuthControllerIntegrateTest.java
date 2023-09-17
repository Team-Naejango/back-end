package com.example.naejango.global.auth.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@ActiveProfiles("Test")
class AuthControllerIntegrateTest extends RestDocsSupportTest {
    @MockBean
    RefreshTokenRepository refreshTokenRepositoryMock;
    @MockBean
    JwtCookieHandler jwtCookieHandlerMock;

    @Nested
    @DisplayName("게스트 회원")
    class guest {
        @Test
        @Tag("api")
        @DisplayName("성공")
        void test1 () throws Exception {
            // given
            BDDMockito.given(jwtCookieHandlerMock.hasRefreshTokenCookie(any()))
                    .willReturn(false);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/auth/guest"));

            // then
            resultActions.andExpect(MockMvcResultMatchers
                    .status().isOk()
            );
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("message")
                    .value("게스트용 토큰이 발급되었습니다."));


            // RestDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("시큐리티")
                            .summary("게스트 회원 생성")
                            .responseFields(
                                    fieldWithPath("message").description("결과 메세지"),
                                    fieldWithPath("result").description("재발급 된 엑세스 토큰")
                            )
                            .build()
                    )));
        }
    }

}