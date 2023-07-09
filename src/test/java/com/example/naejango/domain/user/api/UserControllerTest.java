//package com.example.naejango.domain.user.api;
//
//import com.example.naejango.domain.user.application.UserService;
//import com.example.naejango.domain.user.domain.Gender;
//import com.example.naejango.domain.user.domain.User;
//import com.example.naejango.domain.user.domain.UserProfile;
//import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
//import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
//import com.example.naejango.domain.user.dto.response.UserProfileResponseDto;
//import com.example.naejango.global.common.handler.CommonDtoHandler;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.mockito.BDDMockito;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.servlet.http.HttpServletRequest;
//
//import static org.mockito.Mockito.*;
//
//
//@WebMvcTest(UserController.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class UserControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//    @MockBean
//    UserService userServiceMock;
//
//    @MockBean
//    CommonDtoHandler commonDtoHandlerMock;
//
//    @Test
//    @DisplayName("userProfile Test")
//    @WithMockUser()
//    void userProfileTest() throws Exception {
//        //given
//        UserProfile testUserProfile = UserProfile.builder().nickname("Nick").build();
//        BDDMockito.given(userServiceMock.findUser(any(Long.class))).willReturn(
//                User.builder()
//                        .userProfile(testUserProfile)
//                        .build()
//        );
//
//        // when
//        mockMvc.perform(MockMvcRequestBuilders
//                        .get("http://localhost:8080/api/user/info"))
//        // then
//                .andExpect(
//                        MockMvcResultMatchers
//                        .status().isOk())
//                .andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.nickname").value(testUserProfile.getNickname())
//                )
//                .andDo(MockMvcResultHandlers.print());
//    }
//
//    @Test
//    @Transactional
//    @DisplayName("createUserProfile Test")
//    public void createUserProfileTest() throws Exception {
//        // given
//        CreateUserProfileRequestDto requestDto = new CreateUserProfileRequestDto
//                (20,
//                        Gender.Male,
//                "Nick",
//                "Hello",
//                "010-0000-0000",
//                "http://www.naver.com");
//        ObjectMapper objectMapper = new ObjectMapper();
//        String requestJson = objectMapper.writeValueAsString(requestDto);
//
//        BDDMockito.given(commonDtoHandlerMock.userIdFromAuthentication(any(Authentication.class)))
//                .willReturn(1L);
//
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders
//                        .post("http://localhost:8080/api/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson)
//                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
//                .andExpect(MockMvcResultMatchers
//                        .status().isOk());
//    }
//
//    @Test
//    @DisplayName("modifyUserProfile Test")
//    @WithMockUser()
//    void modifyUserProfileTest() throws Exception {
//        // given
//        ModifyUserProfileRequestDto modifyRequest =
//                ModifyUserProfileRequestDto.builder()
//                        .nickname("nickname")
//                        .imgUrl("imgUrl")
//                        .intro("intro")
//                        .build();
//        ObjectMapper objectMapper = new ObjectMapper();
//        String modifyRequestJson = objectMapper.writeValueAsString(modifyRequest);
//
//        UserProfile testUserProfile = getUserProfile("nickname", "imgUrl", 20, Gender.Male, "intro");
//
//        BDDMockito.given(userServiceMock.getUserInfo(any(Authentication.class))).willReturn(
//                UserProfileResponseDto.builder()
//                        .userProfile(testUserProfile)
//                        .build()
//        );
//
//        // when
//        mockMvc.perform(MockMvcRequestBuilders
//                        .patch("http://localhost:8080/api/user/info")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(modifyRequestJson)
//                        .with(SecurityMockMvcRequestPostProcessors.csrf())
//                )
//
//        // then
//                .andExpect(MockMvcResultMatchers
//                        .status().isOk())
//                .andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.nickname").value(testUserProfile.getNickname())
//                ).andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.imgUrl").value(testUserProfile.getImgUrl())
//                ).andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.age").value(testUserProfile.getAge())
//                ).andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.gender").value(testUserProfile.getGender().name())
//                ).andExpect(
//                        MockMvcResultMatchers
//                                .jsonPath("$.intro").value(testUserProfile.getIntro())
//                )
//                .andDo(MockMvcResultHandlers.print());
//    }
//
//    @Test
//    @DisplayName("deleteUserTest : Status 200 Ok return")
//    @WithMockUser()
//    void deleteUser() throws Exception {
//        // given
//        BDDMockito.given(userServiceMock.deleteUser(any(Authentication.class), any(HttpServletRequest.class)))
//                .willReturn(ResponseEntity.ok().build());
//
//        // when
//        mockMvc.perform(MockMvcRequestBuilders
//                        .delete("http://localhost:8080/api/user/")
//                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
//
//        // then
//                .andExpect(MockMvcResultMatchers
//                        .status().isOk())
//                .andDo(MockMvcResultHandlers.print());
//
//    }
//
//    private UserProfile getUserProfile(String nickname, String imgUrl, int age, Gender gender, String intro) {
//        UserProfile testUserProfile = UserProfile.builder()
//                .nickname(nickname)
//                .imgUrl(imgUrl)
//                .age(age)
//                .gender(gender)
//                .intro(intro)
//                .build();
//        return testUserProfile;
//    }
//
//}