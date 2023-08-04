package com.example.naejango.global.common.handler;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.example.naejango.global.auth.principal.PrincipalDetails;
@Component
public class CommonDtoHandler {
    public Long userIdFromAuthentication(Authentication authentication) {
        if(authentication == null) return null;
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser().getId();
    }
}
