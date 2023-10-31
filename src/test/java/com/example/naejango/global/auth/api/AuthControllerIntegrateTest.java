package com.example.naejango.global.auth.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@ActiveProfiles("Test")
class AuthControllerIntegrateTest extends RestDocsSupportTest {

    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("게스트 회원")
    class guest {

        @Test
        @DisplayName("성공")
        void test1 () throws Exception {
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

    @Nested
    @Transactional
    @DisplayName("공용 회원 로그인")
    class commonUser {

        @Test
        @DisplayName("성공")
        void test1() throws Exception {
            // given
            userRepository.save(User.builder()
                    .role(Role.COMMON)
                    .userKey("Common")
                    .password("")
                    .build());

            em.flush(); em.clear();
            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/auth/common-user"));

            // then
            resultActions.andExpect(MockMvcResultMatchers
                    .status().isOk()
            );
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("message")
                    .value("공용 유저의 토큰이 발급되었습니다."));


            // RestDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("시큐리티")
                            .summary("공용 회원 토큰 발급")
                            .responseFields(
                                    fieldWithPath("message").description("결과 메세지"),
                                    fieldWithPath("result").description("재발급 된 엑세스 토큰")
                            )
                            .build()
                    )));
        }

    }



}