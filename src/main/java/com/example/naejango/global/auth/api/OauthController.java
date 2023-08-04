package com.example.naejango.global.auth.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.GuestTokenResponse;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OauthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CommonDtoHandler commonDtoHandler;
    /**
     * 로그인 테스트를 위한 Controller
     */

    @GetMapping("/oauthtest")
    public String oauthtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://43.202.25.203:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }

    @GetMapping("/localtest")
    public String localtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://localhost:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }


    /**
     * 둘러보기 회원 (게스트) 용 jwt 발급 api
     * */

    @GetMapping("/guest")
    public ResponseEntity<GuestTokenResponse> guest(HttpServletResponse response, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        if (userId != null) return ResponseEntity.ok().body(null);
        User guest = userRepository.findByUserKey("Guest").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = JWT.create()
                .withClaim("userId", guest.getId())
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        String refreshToken = JWT.create()
                .withClaim("userId", guest.getId())
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        userService.refreshSignature(guest.getId(), refreshToken);
        response.addCookie(new Cookie(JwtProperties.REFRESH_TOKEN_COOKIE, refreshToken));

        GuestTokenResponse tokenResponse = GuestTokenResponse.builder().AccessToken(accessToken).build();

        return ResponseEntity.ok().body(tokenResponse);
    }


}