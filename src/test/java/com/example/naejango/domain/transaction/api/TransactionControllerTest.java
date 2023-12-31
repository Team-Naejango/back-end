package com.example.naejango.domain.transaction.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.dto.CreateChannelDto;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.transaction.application.TransactionService;
import com.example.naejango.domain.transaction.domain.TransactionStatus;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionCommandDto;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionDataResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.ModifyTransactionResponseDto;
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

@WebMvcTest(TransactionController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class TransactionControllerTest extends RestDocsSupportTest {

    @MockBean
    TransactionService transactionService;
    @MockBean
    WebSocketService webSocketService;
    @MockBean
    ChannelService channelService;
    @MockBean
    MessageService messageService;
    @MockBean
    AuthenticationHandler authenticationHandler;

    @Nested
    @Order(1)
    @DisplayName("Controller 거래 내역 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findTransactionList {
        Long userId;
        List<FindTransactionResponseDto> findTransactionResponseDtoList =
                new ArrayList<>(List.of(
                        new FindTransactionResponseDto(1L, "2023-07-15T17:35", -1000, "구매", "거래 약속", 1L, "거래자 이름1", "아이템 이름1", 1L),
                        new FindTransactionResponseDto(2L, "2023-06-23T15:16", 2000, "판매", "거래 완료", 2L, "거래자 이름2", "아이템 이름2", 2L)
                ));
        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("거래_내역_조회")
        void 거래_내역_조회() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.findTransactionList(userId))
                    .willReturn(findTransactionResponseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/transaction")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .description("거래 내역 조회")
                                    .responseFields(
                                            fieldWithPath("result[].id").description("거래 ID"),
                                            fieldWithPath("result[].date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("result[].amount").description("거래 금액"),
                                            fieldWithPath("result[].status").description("구매 or 판매"),
                                            fieldWithPath("result[].progress").description("거래 예약 or 송금 완료 or 거래 완료"),
                                            fieldWithPath("result[].traderId").description("거래자 ID"),
                                            fieldWithPath("result[].traderName").description("거래자 이름"),
                                            fieldWithPath("result[].itemName").description("아이템 이름"),
                                            fieldWithPath("result[].itemId").description("아이템 ID"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("거래 내역 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Controller 특정 거래 정보 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findTransactionById {
        Long userId, transactionId=1L;
        FindTransactionResponseDto findTransactionResponseDto =
                new FindTransactionResponseDto(1L, "2023-07-15T17:35", -1000, "구매", "거래 완료", 1L, "거래자 이름1", "아이템 이름1", 1L);

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("특정_거래_정보_조회")
        void 특정_거래_정보_조회() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.findTransactionById(userId, transactionId))
                    .willReturn(findTransactionResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/transaction/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("특정 거래 정보 조회")
                                    .description("- 해당 거래의 정보를 요청한 사람이 거래의 판매자 혹은 구매자가 아니면 예외 처리\n\n" +
                                            "- 요청한 유저가 판매자일 경우 거래 금액의 양수값, 구매자일 경우 거래 금액의 음수값 응답")
                                    .pathParameters(
                                            parameterWithName("transactionId").description("거래 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("거래 ID"),
                                            fieldWithPath("result.date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("result.amount").description("거래 금액"),
                                            fieldWithPath("result.status").description("구매 or 판매"),
                                            fieldWithPath("result.progress").description("거래 예약 or 송금 완료 or 거래 완료"),
                                            fieldWithPath("result.traderId").description("거래자 ID"),
                                            fieldWithPath("result.traderName").description("거래자 이름"),
                                            fieldWithPath("result.itemName").description("아이템 이름"),
                                            fieldWithPath("result.itemId").description("아이템 ID"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("특정 거래 정보 조회 Response"))
                                    .build()
                    )));
        }
    }
    @Nested
    @Order(3)
    @DisplayName("Controller 상대 유저와 완료 되지 않은 거래 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findTransactionByTraderId {
        Long userId=1L, traderId=1L;
        List<FindTransactionDataResponseDto> responseDtoList =
                new ArrayList<>(List.of(
                        new FindTransactionDataResponseDto(1L, "2023-07-15T17:35", 1000, TransactionStatus.TRANSACTION_APPOINTMENT),
                        new FindTransactionDataResponseDto(2L, "2023-06-23T15:16", 2000, TransactionStatus.REMITTANCE_COMPLETION)
                ));
        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("상대_유저와_완료_되지_않은_거래_조회")
        void 상대_유저와_완료_되지_않은_거래_조회() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.findTransactionByTraderId(userId, traderId))
                    .willReturn(responseDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/transaction/trader/{traderId}", traderId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("거래")
                                    .summary("상대 유저와 완료 되지 않은 거래 조회")
                                    .description("- 두 유저 사이의 거래 중 완료되지 않은 거래가 있다면 리스트로 반환, 없다면 빈 리스트\n\n"+
                                            "- 해당 API에서 status는 2개밖에 없음\n\n" +
                                            "   'TRANSACTION_APPOINTMENT(거래 약속)', 'REMITTANCE_COMPLETION(송금 완료)'\n\n")
                                    .pathParameters(
                                            parameterWithName("traderId").description("상대 유저 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result[].id").description("거래 ID"),
                                            fieldWithPath("result[].date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("result[].amount").description("거래 금액"),
                                            fieldWithPath("result[].status").description("거래 상태"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("상대 유저와 완료 되지 않은 거래 조회 Response"))
                                    .build()
                    )));
        }
    }
    @Nested
    @Order(4)
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

        CreateTransactionCommandDto createTransactionCommandDto =
                new CreateTransactionCommandDto(createTransactionRequestDto);

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

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.createTransaction(userId, createTransactionCommandDto))
                    .willReturn(createTransactionResponseDto);
            BDDMockito.given(channelService.createPrivateChannel(userId, createTransactionCommandDto.getTraderId()))
                    .willReturn(new CreateChannelDto(false, 0L, 0L));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/transaction")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("result.id").isNumber());
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
                                            fieldWithPath("result.id").description("거래 ID"),
                                            fieldWithPath("result.date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("result.amount").description("거래 금액"),
                                            fieldWithPath("result.status").description("거래 상태 ex) TRANSACTION_APPOINTMENT"),
                                            fieldWithPath("result.userId").description("유저 ID"),
                                            fieldWithPath("result.traderId").description("거래자 ID"),
                                            fieldWithPath("result.itemId").description("아이템 ID"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("거래 등록 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(5)
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
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/{transactionId}/remit", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
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
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("송금 완료 요청 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(6)
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
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/{transactionId}/complete", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
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
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("송금 완료 요청 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(7)
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

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(transactionService.modifyTransaction(any(), any(), any()))
                    .willReturn(modifyTransactionResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/transaction/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
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
                                            fieldWithPath("result.date").description("거래 날짜 및 시간 ex) 2023-08-10T15:30"),
                                            fieldWithPath("result.amount").description("거래 금액"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("거래 정보 수정 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(8)
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
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/transaction/{transactionId}", transactionId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
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
                                    .responseFields(
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("거래 삭제 Response"))
                                    .build()
                    )));
        }
    }
}