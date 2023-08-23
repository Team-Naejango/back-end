package com.example.naejango.domain.transaction.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.transaction.application.TransactionService;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.ModifyTransactionResponseDto;
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

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(TransactionController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class TransactionControllerTest extends RestDocsSupportTest {
    @MockBean
    TransactionService transactionService;
    @MockBean
    CommonDtoHandler commonDtoHandler;

    @Nested
    @Order(1)
    @DisplayName("Controller 거래 내역 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findTransaction {
        Long userId;
        List<FindTransactionResponseDto> findTransactionResponseDtoList =
                new ArrayList<>(List.of(
                        new FindTransactionResponseDto(1L, "2023-07-15T17:35", 1000, "구매", "거래자 이름1", "아이템 이름1", 1L),
                        new FindTransactionResponseDto(2L, "2023-06-23T15:16", 2000, "판매", "거래자 이름2", "아이템 이름2", 2L)
                ));
        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_내역_조회")
        void 거래_내역_조회() throws Exception {
            // given
            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.findTransaction(userId))
                    .willReturn(findTransactionResponseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/transaction")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .description("거래 내역 조회")
                                    .responseFields(
                                            fieldWithPath("[].id").description("거래 ID"),
                                            fieldWithPath("[].date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("[].amount").description("거래 금액"),
                                            fieldWithPath("[].status").description("구매 or 판매"),
                                            fieldWithPath("[].traderName").description("거래자 이름"),
                                            fieldWithPath("[].itemName").description("아이템 이름"),
                                            fieldWithPath("[].itemId").description("아이템 ID")
                                    )
                                    .responseSchema(Schema.schema("거래 내역 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Controller 거래 등록")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class createTransaction {
        Long userId;

        CreateTransactionRequestDto createTransactionRequestDto =
                CreateTransactionRequestDto.builder()
                        .date("2023-08-10T15:30")
                        .amount(3000)
                        .traderId(1L)
                        .itemId(1L)
                        .build();

        CreateTransactionResponseDto createTransactionResponseDto =
                CreateTransactionResponseDto.builder()
                        .id(1L)
                        .date("2023-08-10T15:30")
                        .amount(3000)
                        .status("TRANSACTION_APPOINTMENT")
                        .userId(1L)
                        .traderId(1L)
                        .itemId(1L)
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_등록")
        void 거래_등록() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createTransactionRequestDto);

            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.createTransaction(userId, createTransactionRequestDto))
                    .willReturn(createTransactionResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/transaction")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("id").isNumber());
            resultActions.andExpect(MockMvcResultMatchers.header().exists("Location"));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("거래 등록")
                                    .description("거래 등록 요청은 판매자 입장에서만 가능\n\n" +
                                            "거래 등록 시 응답의 status는 생성된 직후 이므로 항상 TRANSACTION_APPOINTMENT\n\n" +
                                            "date는 포맷 잘 맞춰서 string 값으로 설정"
                                    )
                                    .requestFields(
                                            fieldWithPath("date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("amount").description("거래 금액"),
                                            fieldWithPath("traderId").description("거래자 ID"),
                                            fieldWithPath("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("id").description("거래 ID"),
                                            fieldWithPath("date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("amount").description("거래 금액"),
                                            fieldWithPath("status").description("거래 상태 ex) TRANSACTION_APPOINTMENT"),
                                            fieldWithPath("userId").description("유저 ID"),
                                            fieldWithPath("traderId").description("거래자 ID"),
                                            fieldWithPath("itemId").description("아이템 ID")
                                    )
                                    .responseSchema(Schema.schema("거래 등록 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Controller 송금 완료 요청")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class waitTransaction {
        Long userId;
        Long transactionId = 1L;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("송금_완료_요청")
        void 송금_완료_요청() throws Exception {
            // given
            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/remittance/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("송금 완료 요청")
                                    .description("송금 완료 요청은 구매자 입장에서만 가능")
                                    .pathParameters(
                                            parameterWithName("transactionId").description("거래 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("송금 완료 요청 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Controller 거래 완료 요청")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class completeTransaction {
        Long userId;
        Long transactionId = 1L;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_완료_요청")
        void 거래_완료_요청() throws Exception {
            // given
            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/completion/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("거래 완료 요청")
                                    .description("거래 완료 요청은 판매자 입장에서만 가능")
                                    .pathParameters(
                                            parameterWithName("transactionId").description("거래 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("status").description("상태코드"),
                                            fieldWithPath("message").description("메시지")
                                    )
                                    .responseSchema(Schema.schema("송금 완료 요청 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Controller 거래 정보 수정")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class modifyTransaction {
        Long userId;
        Long transactionId = 1L;
        ModifyTransactionRequestDto modifyTransactionRequestDto =
                ModifyTransactionRequestDto.builder()
                        .date("2023-08-10T15:30")
                        .amount(3000)
                        .build();

        ModifyTransactionResponseDto modifyTransactionResponseDto =
                ModifyTransactionResponseDto.builder()
                        .date("2023-08-10T15:30")
                        .amount(3000)
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_정보_수정")
        void 거래_정보_수정() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(modifyTransactionRequestDto);

            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.modifyTransaction(any(), any(), any()))
                    .willReturn(modifyTransactionResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/{transactionId}", transactionId)
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
                                    .tag("거래")
                                    .summary("거래 정보 수정")
                                    .description("거래 정보 수정은 등록한 판매자 입장의 유저만 가능\n\n" +
                                            "거래 정보 수정은 거래 예약 상태에서만 가능 (송금 완료 이후 수정 불가)")
                                    .pathParameters(
                                            parameterWithName("transactionId").description("거래 ID")
                                    )
                                    .requestFields(
                                            fieldWithPath("date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("amount").description("거래 금액")
                                    )
                                    .responseFields(
                                            fieldWithPath("date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("amount").description("거래 금액")
                                    )
                                    .responseSchema(Schema.schema("거래 정보 수정 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(6)
    @DisplayName("Controller 거래 삭제")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class deleteTransaction {
        Long userId;
        Long transactionId = 1L;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_삭제")
        void 거래_삭제() throws Exception {
            // given
            BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/transaction/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("거래 삭제")
                                    .description("거래 삭제는 등록한 판매자 입장의 유저만 가능\n\n" +
                                            "거래 삭제는 거래 예약 상태에서만 가능 (송금 완료 이후 수정 불가)")
                                    .pathParameters(
                                            parameterWithName("transactionId").description("거래 ID")
                                    )
                                    .responseSchema(Schema.schema("거래 삭제 Response"))
                                    .build()
                    )));
        }
    }
}