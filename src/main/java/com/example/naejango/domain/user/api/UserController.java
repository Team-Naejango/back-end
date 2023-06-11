package com.example.naejango.domain.user.api;

import com.example.naejango.domain.user.entity.User;
import com.example.naejango.global.auth.PrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/user")
@RestController
public class UserController {
    @GetMapping("/info")
    public String user(Authentication authentication){
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();
        System.out.println("id = " + user.getId());
        System.out.println("userKey = " + user.getUserKey());
        System.out.println("role = " + user.getRole());
        return "인증에 성공하였습니다.";
    }
}
