package com.example.naejango.global.auth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OauthController {
    /**
     * 로컬 로그인 테스트를 위한 Controller
     */
    @GetMapping("/localtest")
    public String localtest() {
        return "<h1>test</h1>" +
                "<a href=\"http://localhost:8080/oauth2/authorization/kakao\">" +
                "<img height=\"38px\" src=\"https://developers.kakao.com/tool/resource/static/img/button/kakaosync/complete/ko/kakao_login_medium_narrow.png\"></a>";
    }
}
