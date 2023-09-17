package com.example.naejango.global.common.util;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Principal;

@Component
public class AuthenticationHandler {
    public Long getUserId(Authentication authentication) {
        if(authentication == null) {
            throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof PrincipalDetails) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            return principalDetails.getUser().getId();
        }
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
    }

    public Long getUserId(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return getUserId(authentication);
    }

    public User getUser(Authentication authentication) {
        if(authentication == null) {
            throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
        }
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser();
    }

    public Role getRole(Authentication authentication) {
        return getUser(authentication).getRole();
    }
}
