package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * 인증은 되었으나 권한이 없는 요청에 대해 처리합니다.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && isTemporalUser(authentication)) {
            throw new CustomException(ErrorCode.SIGNUP_INCOMPLETE);
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isTemporalUser(Authentication authentication) {
        return ((PrincipalDetails) authentication.getPrincipal()).getRole().equals(Role.TEMPORAL);
    }

}
