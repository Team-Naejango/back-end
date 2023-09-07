package com.example.naejango.domain.notification.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.notification.application.NotificationService;
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

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;

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
                                .description("알림 구독의 ContentType은 text/event-stream\n\nLast-Event-ID는 필수는 아니지만 헤더에 포함시켜 요청하면 이전에 받지 못한 이벤트가 존재 하는 경우 받지 못한 이벤트 부터 받을 수 있음")
                                .requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"),
                                        headerWithName("Last-Event-ID").description("마지막 event의 ID").optional()
                                )
                                .build()
                )));
    }
}