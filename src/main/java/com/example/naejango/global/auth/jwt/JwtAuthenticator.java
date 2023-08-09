package com.example.naejango.global.auth.jwt;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.ValidateTokenResponseDto;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticator {
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;

    /**
     * jwt 검증을 시도하고 그 결과에 따라 authentication 객체를 생성해주는 메서드
     * access token 이 유효한 경우 authentication 생성 후 반환
     * access token 이 없거나 유효하지 않은 경우 refresh token 의 유효성 검증 수행
     * refresh token 이 유효한 경우 access token 을 재발행하여 cookie 로 전달
     * refresh token 이 없거나 유효하지 않은 경우 아무 작업 수행하지 않고 반환
     */
    public void jwtAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = this.getAccessToken(request);

        // access token 이 있는 경우
        if (accessToken != null) {
            // access token 유효성 검증 수행
            ValidateTokenResponseDto ValidateAccessTokenResponseDto = jwtValidator.validateAccessToken(accessToken);
            // access token 이 유효한 경우
            if (ValidateAccessTokenResponseDto.isValidToken()) {
                // Authenticate 진행
                authenticate(ValidateAccessTokenResponseDto.getUserId());
                return;
            }
        }

        String refreshToken = this.getRefreshToken(request);
        // access token 은 없고 refresh token 이 있는 경우
        if (refreshToken != null) {
            // refresh token 유효성 검증 수행
            ValidateTokenResponseDto refreshValidateTokenResponseDto = jwtValidator.validateRefreshToken(refreshToken);
            // refresh token 이 유효한 경우
            if (refreshValidateTokenResponseDto.isValidToken()) {
                // access token 을 재발행하여 cookie 로 응답
                reissueAccessToken(response, refreshValidateTokenResponseDto.getUserId());
                // authenticate 진행
                authenticate(refreshValidateTokenResponseDto.getUserId());
            }
        }

    }

    /**
     * access token 이 없거나 유효하지 않은데
     * refresh token 만 유효한 경우
     * access token 을 재 발행하여 쿠키에 담아 반환
     */
    private void reissueAccessToken(HttpServletResponse response, Long userId) {
        String reissuedAccessToken = jwtGenerator.generateAccessToken(userId);
        Cookie accessTokenCookie = new Cookie("AccessToken", reissuedAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
    }

    /**
     * authenticate
     * Authentication 객체를 생성하여 SecurityContext 에 넣음
     * Authentication : UsernamePasswordAuthenticationToken
     * exception : jwtToken을 지니고 있는데 해당 회원이 없는 경우
     */
    private void authenticate (Long userId){
        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomException(ErrorCode.USER_NOT_FOUND));
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * HttpServletRequest 에서 access token 을 가져오는 메서드
     * Header 에 access token 이 없거나 유효하지 않은 형태인 경우 null 을 반환
     */
    private String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
        }
        return null;
    }

    /**
     * HttpServletRequest 에서 refresh token 을 가져오는 메서드
     * refresh cookie 가 없는 경우 null 을 반환
     */
    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findAny()
                .orElse(null);
    }

}
