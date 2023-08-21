package com.example.naejango.domain.notification.api;

import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.notification.application.NotificationService;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest extends RestDocsSupportTest {
    @MockBean
    NotificationService notificationService;

    @MockBean
    CommonDtoHandler commonDtoHandler;

    @Test
    @Tag("api")
    @DisplayName("알림_구독_요청")
    void 알림_구독_요청() throws Exception {
        // given
        Long userId = 1L;
        BDDMockito.given(commonDtoHandler.userIdFromAuthentication(any()))
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
    }
}