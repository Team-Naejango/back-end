package com.example.naejango.domain.chat.application.http;

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
import com.example.naejango.domain.user.application.UserService;
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
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.util.GeomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatServiceTest {
    @Autowired EntityManager em;
    @Autowired UserService userService;
    @Autowired JwtGenerator jwtGenerator;
    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StorageRepository storageRepository;
    @Autowired StorageService storageService;
    @Autowired ChannelRepository channelRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired ChatRepository chatRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired ChatMessageRepository chatMessageRepository;
    @Autowired ChatService chatService;
    @Autowired FollowRepository followRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired JwtValidator jwtValidator;
    @Autowired GeomUtil geomUtil;
    User user, otherUser;
    UserProfile userProfile1, userProfile2;
    Follow follow;Account account;Storage storage;Item item;
    Channel groupChannel, privateChannel, openGroupChannel;
    Chat chat1, chat2, chat3, chat4, chat5, chat6;
    Message message1, message2, message3, message4;
    ChatMessage chatMessage1, chatMessage2, chatMessage3, chatMessage4, chatMessage5, chatMessage6, chatMessage7, chatMessage8;
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

        openGroupChannel = GroupChannel.builder().channelType(ChannelType.GROUP).owner(user).defaultTitle("공동구매 대화방").isClosed(false).channelLimit(5).participantsCount(2).item(item).build();
        groupChannel = GroupChannel.builder().channelType(ChannelType.GROUP).owner(user).defaultTitle("공동구매 대화방").isClosed(true).channelLimit(5).participantsCount(2).item(item).build();
        privateChannel = PrivateChannel.builder().channelType(ChannelType.PRIVATE).isClosed(false).build();
        channelRepository.save(openGroupChannel);channelRepository.save(groupChannel);channelRepository.save(privateChannel);

        chat1 = Chat.builder().owner(user).channel(groupChannel).title("그룹챗1").build();
        chat2 = Chat.builder().owner(otherUser).channel(groupChannel).title("그룹챗2").build();
        chat3 = Chat.builder().owner(user).channel(privateChannel).title("개인챗1").build();
        chat4 = Chat.builder().owner(otherUser).channel(privateChannel).title("개인챗1").build();
        chat5 = Chat.builder().owner(user).channel(openGroupChannel).title("열린챗").build();
        chat6 = Chat.builder().owner(otherUser).channel(openGroupChannel).title("열린챗").build();
        chatRepository.save(chat1);chatRepository.save(chat2);chatRepository.save(chat3);chatRepository.save(chat4);chatRepository.save(chat5);chatRepository.save(chat6);

        message1 = Message.builder().channel(groupChannel).senderId(user.getId()).messageType(MessageType.CHAT).content("메세지1").build();
        message2 = Message.builder().channel(groupChannel).senderId(otherUser.getId()).messageType(MessageType.CHAT).content("메세지2").build();
        message3 = Message.builder().channel(privateChannel).senderId(user.getId()).messageType(MessageType.CHAT).content("메세지3").build();
        message4 = Message.builder().channel(privateChannel).senderId(otherUser.getId()).messageType(MessageType.CHAT).content("메세지4").build();
        messageRepository.save(message1);messageRepository.save(message2);messageRepository.save(message3);messageRepository.save(message4);

        chatMessage1 = ChatMessage.builder().message(message1).chat(chat1).isRead(true).build();
        chatMessage2 = ChatMessage.builder().message(message1).chat(chat2).isRead(true).build();
        chatMessage3 = ChatMessage.builder().message(message2).chat(chat1).isRead(true).build();
        chatMessage4 = ChatMessage.builder().message(message2).chat(chat2).isRead(true).build();
        chatMessage5 = ChatMessage.builder().message(message3).chat(chat3).isRead(true).build();
        chatMessage6 = ChatMessage.builder().message(message3).chat(chat4).isRead(true).build();
        chatMessage7 = ChatMessage.builder().message(message4).chat(chat3).isRead(true).build();
        chatMessage8 = ChatMessage.builder().message(message4).chat(chat4).isRead(true).build();
        chatMessageRepository.save(chatMessage1);chatMessageRepository.save(chatMessage2);
        chatMessageRepository.save(chatMessage3);chatMessageRepository.save(chatMessage4);
        chatMessageRepository.save(chatMessage5);chatMessageRepository.save(chatMessage6);
        chatMessageRepository.save(chatMessage7);chatMessageRepository.save(chatMessage8);

        refreshToken = jwtGenerator.generateRefreshToken(new JwtPayload(user.getId(), user.getRole()));
        refreshTokenRepository.saveRefreshToken(user.getId(), refreshToken);
    }

    @Nested
    @DisplayName("그룹 챗 삭제")
    class DeleteGroupChatTest {
        @Test
        @DisplayName("방장, 채널 진행중인 경우")
        void test1() {
            // when, then
            assertThrows(CustomException.class,
                    () -> chatService.deleteChatByChannelIdAndUserId(openGroupChannel.getId(), user.getId())
            );
        }

        @Test
        @DisplayName("방장인 경우, 채널 종료 됨")
        void test2(){
            // when
            chatService.deleteChatByChannelIdAndUserId(groupChannel.getId(), user.getId());

            // then
            // 챗 삭제
            assertTrue(chatRepository.findById(chat1.getId()).isEmpty());
            // 인원 수 감소
            assertEquals(1, ((GroupChannel)groupChannel).getParticipantsCount());
            // 퇴장 메세지 발행
            assertEquals(messageRepository.findRecentMessages(chat2.getId(), PageRequest.of(0, 2))
                    .getContent().get(0).getMessageType(), MessageType.EXIT);
        }

        @Test
        @DisplayName("방장 아님")
        void test3(){
            // when
            chatService.deleteChatByChannelIdAndUserId(openGroupChannel.getId(), otherUser.getId());

            // then
            // 챗 삭제
            assertTrue(chatRepository.findById(chat6.getId()).isEmpty());
            // 인원 수 감소
            assertEquals(1, ((GroupChannel)openGroupChannel).getParticipantsCount());
            // 퇴장 메세지 발행
            assertEquals(messageRepository.findRecentMessages(chat5.getId(), PageRequest.of(0, 2))
                    .getContent().get(0).getMessageType(), MessageType.EXIT);
        }

        @Test
        @DisplayName("방에 아무도 남지 않게 된 경우")
        void test4(){
            // when
            chatService.deleteChatByChannelIdAndUserId(groupChannel.getId(), user.getId());
            chatService.deleteChatByChannelIdAndUserId(groupChannel.getId(), otherUser.getId());

            // then
            // 챗 삭제
            assertTrue(chatRepository.findById(chat1.getId()).isEmpty());
            assertTrue(chatRepository.findById(chat2.getId()).isEmpty());
            // 채널 삭제
            assertTrue(channelRepository.findById(groupChannel.getId()).isEmpty());
            // 메세지 삭제
            assertFalse(messageRepository.findById(message1.getId()).isEmpty());
        }
    }

    @Nested
    @DisplayName("개인 챗 삭제")
    class DeletePrivateChatTest {
        @Test
        @DisplayName("삭제")
        void test1() {
            // given
            assertEquals(2, chatMessageRepository.findByChatId(chat3.getId()).size());

            // when
            chatService.deleteChatByChannelIdAndUserId(privateChannel.getId(), user.getId());
            em.flush();

            // then
            // 챗 메세지만 삭제됨
            assertTrue(chatRepository.findById(chat3.getId()).isPresent());
            assertTrue(chatMessageRepository.findByChatId(chat3.getId()).isEmpty());
        }
    }


}