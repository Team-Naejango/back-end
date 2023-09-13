package com.example.naejango.domain.transaction.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.dto.request.NotificationRequestDto;
import com.example.naejango.domain.transaction.domain.Transaction;
import com.example.naejango.domain.transaction.domain.TransactionStatus;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.ModifyTransactionResponseDto;
import com.example.naejango.domain.transaction.repository.TransactionRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 거래 내역 조회 */
    public List<FindTransactionResponseDto> findTransaction(Long userId){
        List<Transaction> transactionList = transactionRepository.findByUserIdOrTraderId(userId, userId);

        List<FindTransactionResponseDto> findTransactionResponseDtoList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            findTransactionResponseDtoList.add(new FindTransactionResponseDto(transaction, userId));
        }

        return findTransactionResponseDtoList;
    }

    /** 거래 예약 등록 */
    @Transactional
    public CreateTransactionResponseDto createTransaction(Long userId, CreateTransactionRequestDto createTransactionRequestDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User trader = userRepository.findById(createTransactionRequestDto.getTraderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Item item = itemRepository.findById(createTransactionRequestDto.getItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        Transaction transaction = createTransactionRequestDto.toEntity(user, trader, item);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // url 부분은 수정 해야함
        eventPublisher.publishEvent(new NotificationRequestDto(trader, NotificationType.TRANSACTION, "거래 요청 알림", ""));
        return new CreateTransactionResponseDto(savedTransaction);
    }

    /** 송금 완료로 수정 */
    @Transactional
    public void waitTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getTrader().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        Account userAccount = accountRepository.findByUserId(transaction.getUser().getId()).orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account traderAccount = accountRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        userAccount.chargeBalance(transaction.getAmount());
        traderAccount.deductBalance(transaction.getAmount());
        transaction.waitTransaction();
    }

    /** 거래 완료로 수정 */
    @Transactional
    public void completeTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        transaction.completeTransaction();
    }

    /** 거래 정보 수정(거래일시, 거래 금액) 거래 예약 상태에서만 가능 */
    @Transactional
    public ModifyTransactionResponseDto modifyTransaction(Long userId, Long transactionId, ModifyTransactionRequestDto modifyTransactionRequestDto) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getUser().getId(), userId)) { // 요청 보낸 유저와 거래를 등록한 유저가 같은지 확인
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        // 거래 상태가 예약 인지 체크
        if (!transaction.getStatus().equals(TransactionStatus.TRANSACTION_APPOINTMENT)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_MODIFICATION);
        }
        modifyTransactionRequestDto.toEntity(transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new ModifyTransactionResponseDto(savedTransaction);
    }

    /** 거래 삭제 */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        // 거래 상태가 예약 인지 체크
        if (!transaction.getStatus().equals(TransactionStatus.TRANSACTION_APPOINTMENT)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_DELETE);
        }
        transactionRepository.delete(transaction);
    }
}
