package com.example.naejango.domain.account.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EntityManager em;

    @Test
    void createAccount() {

    }

    @Test
    void chargeAccount() {
        // given
        int amount = 10000;
        Account account = Account.builder().balance(0).build();

        BDDMockito.given(accountRepository.findByUserId(any(Long.class))).willReturn(Optional.ofNullable(account));

        // when
        int balance = accountService.chargeAccount(any(Long.class), amount);

        // then
        Assertions.assertEquals(amount, balance);
    }

    @Test
    void getAccount() {
    }
}