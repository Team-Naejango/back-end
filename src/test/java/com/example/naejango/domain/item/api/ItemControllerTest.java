package com.example.naejango.domain.item.api;

import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.RequestCreateItem;
import com.example.naejango.domain.item.dto.response.ResponseCreateItem;
import com.example.naejango.domain.user.application.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;


@WebMvcTest(ItemController.class)
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    ItemService itemService;

    @MockBean
    UserService userService;

    @Nested
    @DisplayName("아이템 생성")
    @WithMockUser()
    class createItem {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            RequestCreateItem requestCreateItem =
                    RequestCreateItem.builder()
                            .category("생필품")
                            .name("아이템 이름")
                            .description("아이템 설명")
                            .imgUrl("아이템 이미지 Url")
                            .type(ItemType.BUY)
                            .StorageId(1L)
                            .build();

            ResponseCreateItem responseCreateItem =
                    ResponseCreateItem.builder().build();

            String content = objectMapper.writeValueAsString(requestCreateItem);

            BDDMockito.given(itemService.createItem(any(), any(RequestCreateItem.class)))
                    .willReturn(responseCreateItem);


            // when
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/item")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andDo(MockMvcResultHandlers.print());
        }

    }
}