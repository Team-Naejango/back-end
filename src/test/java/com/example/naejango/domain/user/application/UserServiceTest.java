package com.example.naejango.domain.user.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.domain.chat.domain.*;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.chat.repository.ChatMessageRepository;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.domain.follow.domain.Follow;
import com.example.naejango.domain.follow.repository.FollowRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtPayload;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {
    @Autowired UserService userService;
    @Autowired JwtGenerator jwtGenerator;
    @Autowired UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    StorageService storageService;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    FollowRepository followRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired JwtValidator jwtValidator;
    @Autowired GeomUtil geomUtil;
    User user, otherUser;
    UserProfile userProfile1, userProfile2;
    Follow follow;
    Account account;
    Storage storage;
    Item item;
    Channel groupChannel, privateChannel;
    Chat chat1, chat2, chat3, chat4;
    Message message;
    ChatMessage chatMessage1, chatMessage2;
    String refreshToken;

    @BeforeEach
    void setup() {
        user = User.builder().userKey("testUser1").role(Role.USER).password("").build();
        otherUser = User.builder().userKey("testUser2").role(Role.USER).password("").build();
        userRepository.save(user);
        userRepository.save(otherUser);

        userProfile1 = UserProfile.builder().nickname("kim").birth("").intro("").imgUrl("").phoneNumber("").gender(Gender.MALE).build();
        userProfile2 = UserProfile.builder().nickname("lee").birth("").intro("").imgUrl("").phoneNumber("").gender(Gender.MALE).build();
        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);
        user.setUserProfile(userProfile1);
        user.setUserProfile(userProfile2);

        account = Account.builder().balance(10000).user(user).build();
        accountRepository.save(account);

        storage = Storage.builder().user(user).name("창고").address("한").location(geomUtil.createPoint(126.0, 37.0)).build();
        storageRepository.save(storage);

        follow = Follow.builder().storage(storage).user(otherUser).build();
        followRepository.save(follow);

        item = Item.builder().itemType(ItemType.GROUP_BUY).tag("태그1 태그2").imgUrl("").description("테스트").viewCount(0).name("공동구매").status(true).user(user).storage(storage).build();
        itemRepository.save(item);

        groupChannel = GroupChannel.builder().channelType(ChannelType.GROUP).owner(user).defaultTitle("공동구매 대화방").isClosed(false).channelLimit(5).participantsCount(2).item(item).build();
        privateChannel = PrivateChannel.builder().channelType(ChannelType.PRIVATE).isClosed(false).build();
        channelRepository.save(groupChannel);
        channelRepository.save(privateChannel);

        chat1 = Chat.builder().owner(user).channel(groupChannel).title("그룹챗1").build();
        chat2 = Chat.builder().owner(otherUser).channel(groupChannel).title("그룹챗2").build();
        chat3 = Chat.builder().owner(user).channel(privateChannel).title("개인챗1").build();
        chat4 = Chat.builder().owner(otherUser).channel(privateChannel).title("개인챗1").build();
        chatRepository.save(chat1);
        chatRepository.save(chat2);
        chatRepository.save(chat3);
        chatRepository.save(chat4);

        message = Message.builder().channel(groupChannel).senderId(user.getId()).messageType(MessageType.CHAT).content("안녕하세요").build();
        messageRepository.save(message);

        chatMessage1 = ChatMessage.builder().message(message).chat(chat1).isRead(true).build();
        chatMessage2 = ChatMessage.builder().message(message).chat(chat2).isRead(true).build();
        chatMessageRepository.save(chatMessage1);
        chatMessageRepository.save(chatMessage2);

        refreshToken = jwtGenerator.generateRefreshToken(new JwtPayload(user.getId(), user.getRole()));
        refreshTokenRepository.saveRefreshToken(user.getId(), refreshToken);
    }

    @Test
    @DisplayName("유저 삭제")
    void test1(){
        // when
        userService.deleteUser(user.getId(), refreshToken);

        // then
        // 유저 삭제 여부
        userRepository.findById(user.getId()).ifPresentOrElse(
                user -> assertEquals(user.getRole(), Role.DELETED),
                Assertions::fail
        );
        // 창고 삭제 여부
        assertTrue(storageRepository.findById(storage.getId()).isEmpty());

        // 채널 닫힘 여부
        Optional<Channel> channel = channelRepository.findById(groupChannel.getId());
        assertTrue(channel.isPresent());
        assertTrue(channel.get().getIsClosed());

        // 아이템 삭제 여부
        assertTrue(itemRepository.findById(item.getId()).isEmpty());

        // 챗 삭제 여부
        assertTrue(chatRepository.findById(chat1.getId()).isEmpty());
    }

}

