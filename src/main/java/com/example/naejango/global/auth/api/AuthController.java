package com.example.naejango.global.auth.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.JwtCookieSetter;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final CommonDtoHandler commonDtoHandler;
    private final JwtCookieSetter jwtCookieSetter;

    /**
     * 현재 가지고 있는 RefreshToken 쿠키(및 AccessToken 쿠키)를 만료시키고
     * User 객체의 Signature 도 null로 설정
     */
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        Arrays.stream(request.getCookies()).forEach(cookie -> {
            System.out.println("쿠키삭제");
            if(cookie.getName().equals("RefreshToken")) {
                jwtCookieSetter.deleteRefreshTokenCookie(cookie, response);
            }
            if (cookie.getName().equals("AccessToken")) {
                jwtCookieSetter.deleteAccessTokenCookie(cookie, response);
            }
        });
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        userService.deleteSignature(userId);
        return ResponseEntity.ok().body(null);
    }


    /**
     * 둘러보기 회원 (게스트) 용 jwt 발급 api
     * jwt 가 이미 있는 경우, 로그인이 되어있다고 판단하여 예외 반환
     */
    @GetMapping("/guest")
    public ResponseEntity<Void> guest(HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_IN);
        }

        Long guestId = userService.createGuest();

        String accessToken = JWT.create()
                .withClaim("userId", guestId)
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        String refreshToken = JWT.create()
                .withClaim("userId", guestId)
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        userService.refreshSignature(guestId, refreshToken);

        jwtCookieSetter.addAccessTokenCookie(accessToken, response);
        jwtCookieSetter.addRefreshTokenCookie(refreshToken, response);

        return ResponseEntity.ok().build();
    }

    /**
     * 로그인 테스트를 위한 Controller
     */
    @GetMapping("/oauthtest")
    public String oauthtest() {
        return "<h1>test</h1>" +
                "<a href=\"https://api.naejango.site/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }

    @GetMapping("/localtest")
    public String localtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://localhost:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }

}