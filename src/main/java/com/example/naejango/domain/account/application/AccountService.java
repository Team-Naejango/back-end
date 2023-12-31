package com.example.naejango.domain.account.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    /** 계좌에 금액 충전 */
    @Transactional
    public int chargeAccount(Long userId, int amount) {
        Account account = findAccountByUserId(userId);
        account.chargeBalance(amount);

        return account.getBalance();
    }

    /** 계좌 잔액 조회 */
    public int getAccount(Long userId){
        Account account = findAccountByUserId(userId);

        return account.getBalance();
    }

    private Account findAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
