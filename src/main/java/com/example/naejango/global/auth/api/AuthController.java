package com.example.naejango.global.auth.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.JwtIssuer;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.TokenException;
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


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AccountService accountService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtCookieHandler jwtCookieHandler;
    private final JwtValidator jwtValidator;
    private final JwtIssuer jwtIssuer;

    /**
     * 현재 가지고 있는 RefreshToken 쿠키(및 AccessToken 쿠키)를 만료시키고
     * User 객체의 Signature 도 null로 설정합니다.
     */
    @GetMapping("/logout")
    public ResponseEntity<CommonResponseDto<Void>> logout(HttpServletRequest request,
                                                          HttpServletResponse response) {
        // 쿠키 삭제
        jwtCookieHandler.deleteAccessTokenCookie(request, response);
        jwtCookieHandler.deleteRefreshTokenCookie(request, response);

        // 리프레시 토큰 삭제
        jwtCookieHandler.getRefreshToken(request)
                .flatMap(jwtValidator::validateRefreshToken)
                .ifPresent(refreshTokenRepository::deleteRefreshToken);

        return ResponseEntity.ok().body(new CommonResponseDto<>("토큰이 정상 삭제되었습니다.", null));
    }

    @GetMapping("/refresh")
    public ResponseEntity<CommonResponseDto<String>> refreshAccessToken(HttpServletRequest request) {
        String reissuedAccessToken = jwtIssuer.reissueAccessToken(request)
                .orElseThrow(() -> new CustomException(ErrorCode.REISSUE_TOKEN_FAILURE));

        return ResponseEntity.ok().body(new CommonResponseDto<>("엑세스 토큰을 재발급 합니다.", reissuedAccessToken));
    }

    /**
     * 둘러보기 회원 (게스트) 용 jwt 발급 api
     * RefreshToken 이 이미 있는 경우 AccessToken 토큰을 재발급 합니다.
     */
    @GetMapping("/guest")
    public ResponseEntity<CommonResponseDto<String>> guest(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           Authentication authentication) {
        if (jwtCookieHandler.hasRefreshTokenCookie(request)){
            jwtCookieHandler.deleteAccessTokenCookie(request, response);
            String reissueAccessToken = jwtIssuer.reissueAccessToken(request)
                    .orElseThrow(() -> new CustomException(ErrorCode.REISSUE_TOKEN_FAILURE));
            jwtCookieHandler.addAccessTokenCookie(reissueAccessToken, response);
            throw new TokenException(ErrorCode.TOKEN_ALREADY_EXIST, reissueAccessToken);
        }

        if (authentication != null) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_IN);
        }

        Long guestId = userService.createGuest();

        // 계좌 생성
        accountService.createAccount(guestId);

        String accessToken = JWT.create()
                .withClaim("userId", guestId)
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_A));

        String refreshToken = JWT.create()
                .withClaim("userId", guestId)
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_B));

        refreshTokenRepository.saveRefreshToken(guestId, refreshToken);
        jwtCookieHandler.addRefreshTokenCookie(refreshToken, response);
        return ResponseEntity.ok().body(new CommonResponseDto<>("게스트용 토큰이 발급되었습니다.", accessToken));
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