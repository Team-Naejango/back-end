package com.example.naejango.global.auth.filter;

import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorResponse;
import com.example.naejango.global.common.exception.TokenErrorResponse;
import com.example.naejango.global.common.exception.TokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ExceptionHandlingFilter implements Filter {

    private final ObjectMapper objectMapper;

    /**
     * Security filter 내부의 Exception 을 핸들링 해주기 위한 필터입니다.
     * Filter 또는 interceptor 내부에서 던져진 예외의 경우
     * @RestControllerAdvice 로 지정된 Exception handler 에 도달하지 않아서
     * 아래와 같이 직접 핸들링 하였습니다.
     */

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            chain.doFilter(request, response);
        } catch (CustomException customException) {
           handleException (httpResponse, ErrorResponse.toResponseEntity(customException.getErrorCode()));
        } catch (TokenException tokenException) {
            handleException(httpResponse, TokenErrorResponse.toResponseEntity(tokenException.getErrorCode(), tokenException.getReissuedAccessToken()));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleException(HttpServletResponse response, Object o) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(o));
    }

}
