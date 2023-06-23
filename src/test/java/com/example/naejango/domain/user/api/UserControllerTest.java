package com.example.naejango.domain.user.api;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.UserInfoModifyRequest;
import com.example.naejango.domain.user.dto.response.UserInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpServletRequest;


@WebMvcTest(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    UserService userService;

    @Test
    @DisplayName("getUserInfoTest : UserInfoResponse Return")
    @WithMockUser()
    void getInfoTest() throws Exception {

        //given
        UserProfile testUserProfile = UserProfile.builder()
                .nickname("nickname")
                .imgUrl("imgUrl")
                .age(20)
                .gender(Gender.Male)
                .intro("intro")
                .build();

        BDDMockito.given(userService.getUserInfo(Mockito.any(Authentication.class))).willReturn(
                UserInfoResponse.builder()
                        .userProfile(testUserProfile)
                        .build()
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("http://localhost:8080/api/user/info"))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())

        // then
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.nickname").value(testUserProfile.getNickname())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.imgUrl").value(testUserProfile.getImgUrl())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.age").value(testUserProfile.getAge())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.gender").value(testUserProfile.getGender().name())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.intro").value(testUserProfile.getIntro())
                )
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("modifyInfoTest : UserInfoResponse Return")
    @WithMockUser()
    void modifyInfoTest() throws Exception {
        // given
        UserInfoModifyRequest modifyRequest =
                UserInfoModifyRequest.builder()
                        .nickname("nickname")
                        .imgUrl("imgUrl")
                        .intro("intro")
                        .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String modifyRequestJson = objectMapper.writeValueAsString(modifyRequest);

        UserProfile testUserProfile = UserProfile.builder()
                .nickname("nickname")
                .imgUrl("imgUrl")
                .age(20)
                .gender(Gender.Male)
                .intro("intro")
                .build();

        BDDMockito.given(userService.getUserInfo(Mockito.any(Authentication.class))).willReturn(
                UserInfoResponse.builder()
                        .userProfile(testUserProfile)
                        .build()
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("http://localhost:8080/api/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modifyRequestJson)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                )

        // then
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.nickname").value(testUserProfile.getNickname())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.imgUrl").value(testUserProfile.getImgUrl())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.age").value(testUserProfile.getAge())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.gender").value(testUserProfile.getGender().name())
                ).andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.intro").value(testUserProfile.getIntro())
                )
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("deleteUserTest : Status 200 Ok return")
    @WithMockUser()
    void deleteUser() throws Exception {
        // given
        BDDMockito.given(userService.deleteUser(Mockito.any(Authentication.class), Mockito.any(HttpServletRequest.class)))
                .willReturn(ResponseEntity.ok().build());

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("http://localhost:8080/api/user/")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))

        // then
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andDo(MockMvcResultHandlers.print());

    }

}