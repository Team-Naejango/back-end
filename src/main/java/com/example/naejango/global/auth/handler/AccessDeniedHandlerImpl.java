package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.global.auth.dto.ReissueAccessTokenResponseDto;
import com.example.naejango.global.auth.jwt.AccessTokenReissuer;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 권한이 없는 요청(403 ERROR)을 처리하는 핸들러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AccessTokenReissuer accessTokenReissuer;

    /**
     * 권한이 없는 요청에 대한 처리
     * 기본적으로 UNAUTHORIZED 에러코드를 반환합니다.
     *
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String reissuedAccessToken = accessTokenReissuer.reissueAccessToken(request);

        if(authentication == null && reissuedAccessToken == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        if (authentication == null && reissuedAccessToken != null) {
            var responseBody = new ReissueAccessTokenResponseDto(ErrorCode.ACCESSTOKEN_EXPIRED, reissuedAccessToken);
            generateResponse(responseBody, response);
            return;
        }

        if (authentication != null && isTemporalUser(authentication)) {
            throw new CustomException(ErrorCode.SIGNUP_INCOMPLETE);
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    private void generateResponse(ReissueAccessTokenResponseDto responseDto, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    private boolean isTemporalUser(Authentication authentication) {
        return ((PrincipalDetails) authentication.getPrincipal()).getRole().equals(Role.TEMPORAL);
    }



}
