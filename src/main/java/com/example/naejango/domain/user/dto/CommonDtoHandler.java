package com.example.naejango.domain.user.dto;

import com.example.naejango.global.auth.principal.PrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CommonDtoHandler {
    public Long getUserIdFromAuthentication(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser().getId();
    }
}
