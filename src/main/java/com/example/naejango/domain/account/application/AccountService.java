package com.example.naejango.domain.account.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.dto.request.ChargeAccountRequestDto;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    /** 계좌 생성 */
    @Transactional
    public void createAccount(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Account account = Account.builder().user(user).balance(0).build();

        accountRepository.save(account);
    }

    /** 계좌에 금액 충전 */
    @Transactional
    public void chargeAccount(Long userId, ChargeAccountRequestDto chargeAccountRequestDto) {
        Account account = accountRepository.findByUserId(userId);
        account.chargeBalance(chargeAccountRequestDto.getAmount());
    }
}
