package com.example.naejango.domain.account.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.account.dto.request.ChargeAccountRequestDto;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.global.common.handler.CommonDtoHandler;
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

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(AccountController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class AccountControllerTest extends RestDocsSupportTest {
    @MockBean
    AccountService accountService;
    @MockBean
    CommonDtoHandler commonDtoHandler;

    @Nested
    @Order(1)
    @DisplayName("Controller 계좌 충전")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class chargeAccount {
        Long userId;
        ChargeAccountRequestDto chargeAccountRequestDto =
                ChargeAccountRequestDto.builder().amount(10000).build();
        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("계좌_금액_충전")
        void 계좌_금액_충전() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(chargeAccountRequestDto);

            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/account")
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
                                    .tag("계좌")
                                    .description("계좌 금액 충전")
                                    .requestHeaders(
                                            headerWithName("Authorization").description("JWT")
                                    )
                                    .requestFields(
                                            fieldWithPath("amount").description("충전 금액")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("계좌 금액 충전 Response"))
                                    .build()
                    )));
        }
    }

}