package com.example.naejango.global.common.handler;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Principal;

@Component
public class AuthenticationHandler {
    public Long userIdFromAuthentication(Authentication authentication) {
        if(authentication == null) {
            throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
        }
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser().getId();
    }

    public Long userIdFromPrincipal(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return userIdFromAuthentication(authentication);
    }

    public User userFromAuthentication(Authentication authentication) {
        if(authentication == null) {
            throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
        }
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser();
    }
}