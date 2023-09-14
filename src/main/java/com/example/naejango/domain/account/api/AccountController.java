package com.example.naejango.domain.account.api;

import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.account.dto.request.ChargeAccountRequestDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/account")
@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final AuthenticationHandler authenticationHandler;

    /** 계좌에 금액 충전 */
    @PatchMapping("")
    public ResponseEntity<CommonResponseDto<Integer>> chargeAccount(Authentication authentication, @RequestBody ChargeAccountRequestDto chargeAccountRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);
        int balance = accountService.chargeAccount(userId, chargeAccountRequestDto.getAmount());

        return ResponseEntity.ok().body(new CommonResponseDto<>("정상적으로 금액이 충전 되었습니다.", balance));
    }
}
