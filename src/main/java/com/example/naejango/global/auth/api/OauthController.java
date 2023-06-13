package com.example.naejango.global.auth.api;

import com.example.naejango.global.auth.application.OauthService;
import com.example.naejango.global.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {
    private final OauthService oauthService;

    /**
     * Kakaotalk 로그인 시
     * Kakao 인증서버로 부터 인가 code를 넘겨 받는 controller
     */
    @GetMapping("/login/oauth/{provider}")
    public ResponseEntity<LoginResponse> login(@PathVariable String provider, @RequestParam String code) {
        System.out.println("OauthController.login");
        LoginResponse loginResponse = oauthService.login(provider, code);
        return ResponseEntity.ok().body(loginResponse);
    }
    @GetMapping("/loginPage")
    public String test() {
        return "<h1>test</h1>" +
                "<a href=\"Https://kauth.kakao.com/oauth/authorize?client_id=a5487753d1cfdb999c88c7fb96f0288b&redirect_uri=http://localhost:8080/login/oauth/kakao&response_type=code\"><img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }
}
