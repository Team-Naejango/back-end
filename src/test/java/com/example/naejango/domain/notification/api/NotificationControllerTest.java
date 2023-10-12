package com.example.naejango.domain.notification.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.notification.application.NotificationService;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.dto.response.NotificationResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest extends RestDocsSupportTest {
    @MockBean
    NotificationService notificationService;

    @MockBean
    AuthenticationHandler authenticationHandler;

    @Test
    @Tag("api")
    @DisplayName("알림_구독_요청")
    void 알림_구독_요청() throws Exception {
        // given
        Long userId = 1L;
        BDDMockito.given(authenticationHandler.getUserId(any()))
                .willReturn(userId);
        BDDMockito.given(notificationService.subscribe(any(), any()))
                .willReturn(new SseEmitter());

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/subscribe")
                .header("Authorization", "JWT")
                .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 구독")
                                .description("알림 구독의 ContentType은 text/event-stream\n\n" +
                                        "Last-Event-ID는 헤더에 포함시켜 요청 하면 이전에 받지 못한 이벤트가 존재 하는 경우 받지 못한 이벤트 부터 받을 수 있음")
                                .requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"),
                                        headerWithName("Last-Event-ID").description("마지막 event의 ID").optional()
                                )
                                .build()
                )));
    }

    @Test
    @Tag("api")
    @DisplayName("알림_목록_조회")
    void 알림_목록_조회() throws Exception {
        // given
        Long userId = 1L;
        List<NotificationResponseDto> responseDtoList =
                new ArrayList<>(List.of(
                        new NotificationResponseDto(1L, "알림 내용1", "알림1 url", true, NotificationType.TRANSACTION),
                        new NotificationResponseDto(2L, "알림 내용2", "알림2 url", false, NotificationType.TRANSACTION)
                ));

        BDDMockito.given(authenticationHandler.getUserId(any()))
                .willReturn(userId);
        BDDMockito.given(notificationService.findNotification(any()))
                .willReturn(responseDtoList);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/notification")
                .header("Authorization", "JWT")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 목록 조회")
                                .description("유저의 수신한 알림 목록 조회")
                                .responseFields(
                                        fieldWithPath("result[].id").description("알림 ID"),
                                        fieldWithPath("result[].content").description("알림 내용"),
                                        fieldWithPath("result[].url").description("알림 url"),
                                        fieldWithPath("result[].isRead").description("알림 읽음 여부(true=읽음, false=안읽음"),
                                        fieldWithPath("result[].notificationType").description("알림 타입(TRANSACTION, CHATTING)"),
                                        fieldWithPath("message").description("결과 메시지")
                                )
                                .build()
                )));
    }

    @Test
    @Tag("api")
    @DisplayName("알림_확인")
    void 알림_확인() throws Exception {
        // given
        Long userId = 1L;
        BDDMockito.given(authenticationHandler.getUserId(any()))
                .willReturn(userId);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/notification/{notificationId}", 1L)
                .header("Authorization", "JWT")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

        resultActions.andDo(restDocs.document(
                resource(
                        ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 확인")
                                .description("알림을 수신한 유저가 알림을 클릭 하면 해당 api를 요청")
                                .responseFields(
                                        fieldWithPath("result").description("null"),
                                        fieldWithPath("message").description("결과 메시지")
                                )
                                .build()
                )));
    }
}