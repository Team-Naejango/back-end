package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.auth.principal.PrincipalDetails;
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
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    private static final String temporalUserRedirectUrl = "/"; // 임시

    /**
     * 권한이 없는 요청은 전부 Custom 403 페이지로 redirect
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication = " + authentication);
        if (authentication != null) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            User user = principalDetails.getUser();
            if (user.getRole() == Role.TEMPORAL) {
                response.sendRedirect(temporalUserRedirectUrl);
                return;
            }
            log.warn("권한이 없는 요청 : User({}), RequestUrl({})",user.getId() ,request.getRequestURI());
        }
        response.sendRedirect("/");
    }
}
