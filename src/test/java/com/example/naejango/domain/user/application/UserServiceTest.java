package com.example.naejango.domain.user.application;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@Import({UserService.class, UserRepository.class, JwtValidator.class, BCryptPasswordEncoder.class})
class UserServiceTest {
      @MockBean
      private UserRepository userRepositoryMock;
      @MockBean
      private JwtValidator jwtValidator;
      @Autowired
      private UserService userService;

      private static User user;

      @BeforeAll
      static void setup() {
            user = User.builder().id(1234L).userKey("test_1234").build();
      }

      @Nested
      @DisplayName("getUserTest")
      class getUserTest {
            @Test
            @DisplayName("arg : String")
            void getUserTest1(){
                  // given
                  BDDMockito.given(userRepositoryMock.findByUserKey(any(String.class)))
                          .willReturn(Optional.ofNullable(user));

                  // when
                  User getUser = userService.getUser(user.getUserKey());

                  // then
                  Assertions.assertEquals(getUser, user);
                  verify(userRepositoryMock).findByUserKey(user.getUserKey());
            }

            @Test
            @DisplayName("arg : Long")
            void getUserTest2(){
                  // given
                  BDDMockito.given(userRepositoryMock.findById(any(Long.class)))
                          .willReturn(Optional.ofNullable(user));

                  // when
                  User getUser = userService.getUser(user.getId());

                  // then
                  Assertions.assertEquals(getUser, user);
                  verify(userRepositoryMock).findById(user.getId());
            }

            @Test
            @DisplayName("arg : Authentication")
            void getUserTest3(){
                  // given
                  BDDMockito.given(userRepositoryMock.findById(any(Long.class)))
                          .willReturn(Optional.ofNullable(user));

                  PrincipalDetails principal = new PrincipalDetails(user);
                  Authentication authentication = new UsernamePasswordAuthenticationToken(
                          principal,
                          null,
                          principal.getAuthorities()
                  );

                  // when
                  User getUser = userService.getUser(authentication);

                  // then
                  Assertions.assertEquals(getUser, user);
                  verify(userRepositoryMock).findById(user.getId());
            }
      }








}