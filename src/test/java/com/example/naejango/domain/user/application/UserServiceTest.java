package com.example.naejango.domain.user.application;

import com.example.naejango.domain.chat.application.http.ChatService;
import com.example.naejango.domain.follow.repository.FollowRepository;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.domain.wish.repository.WishRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
      @InjectMocks
      UserService userServiceMock;
      @Mock
      UserRepository userRepositoryMock;
      @Mock
      UserProfileRepository userProfileRepositoryMock;
      @Mock
      StorageService storageServiceMock;
      @Mock
      ChatService chatServiceMock;
      @Mock
      FollowRepository followRepositoryMock;
      @Mock
      WishRepository wishRepositoryMock;
}