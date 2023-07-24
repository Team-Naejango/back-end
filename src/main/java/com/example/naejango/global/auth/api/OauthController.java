package com.example.naejango.global.auth.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/auth")
@RequiredArgsConstructor
public class OauthController {

    /**
     * 로그인 테스트를 위한 Controller
     */

    @GetMapping("/oauthtest")
    public String oauthtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://43.202.25.203:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }

    @GetMapping("/localtest")
    public String localtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://localhost:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }

}