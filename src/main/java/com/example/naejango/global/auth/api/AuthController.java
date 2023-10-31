package com.example.naejango.global.auth.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.*;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Optional;

import static com.example.naejango.domain.user.domain.Role.COMMON;
import static com.example.naejango.domain.user.domain.Role.GUEST;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtCookieHandler jwtCookieHandler;
    private final JwtValidator jwtValidator;
    private final JwtIssuer jwtIssuer;
    private final JwtGenerator jwtGenerator;

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
                .ifPresent(jwtPayload -> refreshTokenRepository.deleteRefreshToken(jwtPayload.getUserId()));

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
        // 이미 엑세스 토큰이 있는 경우
        if (authentication != null) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_IN);
        }

        Optional<String> refreshTokenOptional = jwtCookieHandler.getRefreshToken(request);

        // 리프레시 토큰이 있는 경우
        if(refreshTokenOptional.isPresent()){
            Optional<JwtPayload> jwtPayload = jwtValidator.validateRefreshToken(refreshTokenOptional.get());

            // 리프레시 토큰이 만료된경우
            if(jwtPayload.isEmpty()){
                jwtCookieHandler.deleteAllTokenCookie(request, response);
                return ResponseEntity.ok().body(new CommonResponseDto<>("로그인 정보가 만료되어 삭제하였습니다.", null));
            }

            // 게스트 회원이 아닌 회원으로 로그인된 상태에서 게스트 로그인 중복 시도
            if(!jwtPayload.get().getRole().equals(GUEST)) throw new CustomException(ErrorCode.LOGIN_DUPLICATION);

            String accessToken = jwtGenerator.generateAccessToken(jwtPayload.get());
            return ResponseEntity.ok().body(new CommonResponseDto<>("게스트 로그인 정보가 남아 있습니다. 엑세스 토큰을 재발급합니다.", accessToken));
        }

        // 게스트 회원 발급
        Long guestId = userService.createGuest();

        JwtPayload jwtPayload = new JwtPayload(guestId, GUEST);
        String accessToken = jwtGenerator.generateAccessToken(jwtPayload, Duration.ofDays(365));
        String refreshToken = jwtGenerator.generateRefreshToken(jwtPayload, Duration.ofDays(365));

        refreshTokenRepository.saveRefreshToken(guestId, refreshToken);
        jwtCookieHandler.addRefreshTokenCookie(refreshToken, response);
        return ResponseEntity.ok().body(new CommonResponseDto<>("게스트용 토큰이 발급되었습니다.", accessToken));
    }

    @GetMapping("/common-user")
    public ResponseEntity<CommonResponseDto<String>> common(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           Authentication authentication) {
        // 이미 엑세스 토큰이 있는 경우
        if (authentication != null) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_IN);
        }

        Optional<String> refreshTokenOptional = jwtCookieHandler.getRefreshToken(request);

        // 리프레시 토큰이 있는 경우
        if(refreshTokenOptional.isPresent()){
            Optional<JwtPayload> jwtPayload = jwtValidator.validateRefreshToken(refreshTokenOptional.get());

            // 리프레시 토큰이 만료된경우
            if(jwtPayload.isEmpty()){
                jwtCookieHandler.deleteAllTokenCookie(request, response);
                return ResponseEntity.ok().body(new CommonResponseDto<>("로그인 정보가 만료되어 삭제하였습니다.", null));
            }

            // 중복 로그인 시도
            throw new CustomException(ErrorCode.LOGIN_DUPLICATION);
        }

        Long commonUserId = userService.getCommonUser().orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtGenerator.generateAccessToken(new JwtPayload(commonUserId, COMMON), Duration.ofDays(1));

        return ResponseEntity.ok().body(new CommonResponseDto<>("공용 유저의 토큰이 발급되었습니다.", accessToken));
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