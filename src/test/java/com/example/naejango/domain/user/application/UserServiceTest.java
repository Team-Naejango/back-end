package com.example.naejango.domain.user.application;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({UserService.class, UserRepository.class})
class UserServiceTest {
      @MockBean
      private UserRepository userRepositoryMock;
      @Autowired
      private UserService userService;
      private static User user;
      @BeforeAll
      static void setup() {
            user = User.builder().id(1234L).userKey("test_1234").build();
      }

}