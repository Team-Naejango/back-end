package com.example.naejango.global.auth.jwt;

import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccessTokenReissuer {

    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final JwtCookieHandler jwtCookieHandler;

    public Optional<String> reissueAccessToken(HttpServletRequest request) {
        Optional<String> refreshTokenOpt = jwtCookieHandler.getRefreshToken(request);
        if(refreshTokenOpt.isEmpty()) return Optional.empty();
        Long userId = jwtValidator.validateRefreshToken(refreshTokenOpt.get())
                .orElseThrow(() -> new CustomException(ErrorCode.REISSUE_TOKEN_FAILURE));
        return Optional.ofNullable(jwtGenerator.generateAccessToken(userId));
    }

}
