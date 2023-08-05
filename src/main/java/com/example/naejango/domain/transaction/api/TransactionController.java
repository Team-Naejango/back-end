package com.example.naejango.domain.transaction.api;

import com.example.naejango.domain.transaction.application.TransactionService;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/transaction")
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final CommonDtoHandler commonDtoHandler;

    /** 거래 내역 조회 */
    @GetMapping("")
    public ResponseEntity<List<FindTransactionResponseDto>> findTransaction(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        List<FindTransactionResponseDto> findTransactionResponseDtoList = transactionService.findTransaction(userId);

        return ResponseEntity.ok().body(findTransactionResponseDtoList);
    }

    /** 거래 요청 등록 */
    @PostMapping("")
    public ResponseEntity<CreateTransactionResponseDto> createTransaction(Authentication authentication, @RequestBody CreateTransactionRequestDto createTransactionRequestDto){
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        CreateTransactionResponseDto createTransactionResponseDto = transactionService.createTransaction(userId, createTransactionRequestDto);

        return ResponseEntity.created(URI.create("/api/transaction/"+createTransactionResponseDto.getId())).body(createTransactionResponseDto);
    }

    /** 거래 완료 대기로 수정 */
    @PatchMapping("/{transactionId}")
    public ResponseEntity<BaseResponseDto> waitTransaction(Authentication authentication, @PathVariable Long transactionId){
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        transactionService.waitTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

    /** 거래 완료로 수정 */
    @PatchMapping("/{transactionId}")
    public ResponseEntity<BaseResponseDto> completeTransaction(Authentication authentication, @PathVariable Long transactionId){
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        transactionService.completeTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

    /** 거래 취소 */
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<BaseResponseDto> deleteTransaction(Authentication authentication, @PathVariable Long transactionId) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        transactionService.deleteTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }
}
