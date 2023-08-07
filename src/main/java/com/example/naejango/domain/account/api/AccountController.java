package com.example.naejango.domain.account.api;

import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.account.dto.request.ChargeAccountRequestDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
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
    private final CommonDtoHandler commonDtoHandler;

    /** 계좌에 금액 충전 */
    @PatchMapping("")
    public ResponseEntity<BaseResponseDto> chargeAccount(Authentication authentication, @RequestBody ChargeAccountRequestDto chargeAccountRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        accountService.chargeAccount(userId, chargeAccountRequestDto);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }
}
