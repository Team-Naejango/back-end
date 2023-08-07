package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.handler.CommonDtoHandler;
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

/**
 * 권한이 없는 요청(403 ERROR)을 처리하는 핸들러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final CommonDtoHandler commonDtoHandler;

    private final String loginPage = "/";
    private final String joinPage = "/"; // 임시

    /**
     * 권한이 없는 요청은 전부 Custom 403 페이지로 redirect
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = commonDtoHandler.userFromAuthentication(authentication);
        // 로그인 되지 않은 회원의 접근
        if (user == null) response.sendRedirect(loginPage);
        // Temporal 회원의 접근
        if (user.getRole() == Role.TEMPORAL) response.sendRedirect(joinPage);
    }
}
