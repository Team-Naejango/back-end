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

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticator {
    private final JwtValidator jwtValidator;
    private final UserRepository userRepository;
    /**
     * jwt 검증을 시도하고 그 결과에 따라 authentication 객체를 생성해주는 메서드
     * access token 이 유효한 경우 authentication 생성
     */
    public void authenticateWithAccessToken(HttpServletRequest request) {
        String accessToken = getAccessToken(request);

        if (accessToken == null) {
            return;
        }

        ValidateTokenResponseDto validateResult = jwtValidator.validateAccessToken(accessToken);
        if (validateResult.isValidToken()){
            authenticate(validateResult.getUserId());
        }
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

}
