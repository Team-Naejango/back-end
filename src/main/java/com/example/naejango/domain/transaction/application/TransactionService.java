package com.example.naejango.domain.transaction.application;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.transaction.domain.Transaction;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionRequestDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionResponseDto;
import com.example.naejango.domain.transaction.repository.TransactionRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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

    /** 거래 내역 조회 */
    public List<FindTransactionResponseDto> findTransaction(Long userId){
        List<Transaction> transactionList = transactionRepository.findByUserId(userId);

        List<FindTransactionResponseDto> findTransactionResponseDtoList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            findTransactionResponseDtoList.add(new FindTransactionResponseDto(transaction));
        }

        return findTransactionResponseDtoList;
    }

    /** 거래 요청 등록 */
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

        return new CreateTransactionResponseDto(savedTransaction);
    }

    /** 거래 완료 대기로 수정 */
    @Transactional
    public void waitTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

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

    /** 거래 취소 */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        transactionRepository.delete(transaction);
    }
}
