package com.example.naejango.domain.transaction.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.transaction.application.TransactionService;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionCommandDto;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionCommandDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.ModifyTransactionResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
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
    private final AuthenticationHandler authenticationHandler;

    /** 거래 내역 조회 */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<FindTransactionResponseDto>>> findTransaction(Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        List<FindTransactionResponseDto> findTransactionResponseDtoList = transactionService.findTransaction(userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", findTransactionResponseDtoList));
    }

    /** 거래 예약 등록 */
    @PostMapping("")
    public ResponseEntity<CommonResponseDto<CreateTransactionResponseDto>> createTransaction(Authentication authentication, @RequestBody CreateTransactionRequestDto createTransactionRequestDto){
        Long userId = authenticationHandler.getUserId(authentication);
        CreateTransactionResponseDto createTransactionResponseDto = transactionService.createTransaction(userId, new CreateTransactionCommandDto(createTransactionRequestDto));

        return ResponseEntity.created(URI.create("/api/transaction/"+createTransactionResponseDto.getId())).body(new CommonResponseDto<>("거래 예약 등록 성공", createTransactionResponseDto));
    }

    /** 송금 완료 요청 */
    @PatchMapping("/{transactionId}/remit")
    public ResponseEntity<CommonResponseDto<Void>> remitTransaction(Authentication authentication, @PathVariable Long transactionId){
        Long userId = authenticationHandler.getUserId(authentication);
        transactionService.remitTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("송금 요청 성공", null));
    }

    /** 거래 완료 요청 */
    @PatchMapping("/{transactionId}/complete")
    public ResponseEntity<CommonResponseDto<Void>> completeTransaction(Authentication authentication, @PathVariable Long transactionId){
        Long userId = authenticationHandler.getUserId(authentication);
        transactionService.completeTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("거래 완료 요청 성공", null));
    }

    /** 거래 정보 수정(거래일시, 거래 금액) 거래 예약 상태에서만 가능 */
    @PatchMapping("/{transactionId}")
    public ResponseEntity<CommonResponseDto<ModifyTransactionResponseDto>> modifyTransaction(Authentication authentication, @PathVariable Long transactionId, @RequestBody ModifyTransactionRequestDto modifyTransactionRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);
        ModifyTransactionResponseDto modifyTransactionResponseDto = transactionService.modifyTransaction(userId, transactionId, new ModifyTransactionCommandDto(modifyTransactionRequestDto));

        return ResponseEntity.ok().body(new CommonResponseDto<>("거래 정보 수정 성공", modifyTransactionResponseDto));
    }

    /** 거래 삭제 */
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteTransaction(Authentication authentication, @PathVariable Long transactionId) {
        Long userId = authenticationHandler.getUserId(authentication);
        transactionService.deleteTransaction(userId, transactionId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("거래 삭제 성공", null));
    }
}
