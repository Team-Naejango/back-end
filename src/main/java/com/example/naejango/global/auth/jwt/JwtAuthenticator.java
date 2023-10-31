package com.example.naejango.global.auth.jwt;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticator {
    private final JwtValidator jwtValidator;
    private final UserService userService;


    /**
     * Http 요청의 Authorization 헤더에 담겨 있는 엑세스 토큰의 검증을 시도하고
     * Security Context 에 authentication 을 생성합니다.
     * @param request Http 요청
     */
    public void authenticateRequest(HttpServletRequest request) {
        jwtValidator.validateRefreshToken(request).ifPresent(this::authenticate);
    }

    /**
     * 엑세스 토큰의 검증을 시도하고 authentication 을 반환합니다.
     * Http Header 에 토큰을 포함할 수 없는 웹소켓 환경에서 STOMP 메세지의 헤더정보를 통해 검증 수행합니다.
     * 참고로 모든 요청에 대하여 interceptor 에서 인증 객체를 로드하고 STOMP 헤더에 주입하는데,
     * Redis 에서 User 객체가 담긴 Authentication 를 직렬화/역직렬화 하는 것은 어려움이 있으며
     * UserId 만 저장해두고 간단한 형태의 WebSocket 전용 Authentication 객체를 만들어 주입하도록 하였음
     * @param accessor Stomp 메세지의 헤더 accessor
     * @return Authentication 객체
     */
    public Authentication authenticateWebSocketRequest(StompHeaderAccessor accessor) {
        JwtPayload jwtPayload = jwtValidator.validateRefreshToken(accessor)
                .orElseThrow(() -> new WebSocketException(ErrorCode.ACCESS_TOKEN_NOT_VALID));
        return getWebSocketPrincipal(jwtPayload.getUserId());
    }

    /**
     * authenticate
     * Authentication 객체를 생성하여 SecurityContext 에 넣음
     * Authentication : UsernamePasswordAuthenticationToken
     * exception : jwtToken을 지니고 있는데 해당 회원이 없는 경우
     */
    private void authenticate (JwtPayload jwtPayload){
        Authentication principal = getPrincipal(jwtPayload.getUserId());
        SecurityContextHolder.getContext().setAuthentication(principal);
    }

    private Authentication getPrincipal (Long userId){
        User user = userService.login(userId);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        return new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
    }

    private Authentication getWebSocketPrincipal(Long userId) {
        User user = userService.webSocketLogin(userId);
        return new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(user),
                null,
                Collections.singletonList(() -> "ROLE_" + user.getRole().toString())
        );
    }

}
