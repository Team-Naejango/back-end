package com.example.naejango.domain.transaction.application;

import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.application.websocket.WebSocketService;
import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.CreateChannelDto;
import com.example.naejango.domain.chat.dto.MessagePublishCommandDto;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.dto.NotificationPublishDto;
import com.example.naejango.domain.transaction.domain.Transaction;
import com.example.naejango.domain.transaction.domain.TransactionStatus;
import com.example.naejango.domain.transaction.dto.request.CreateTransactionCommandDto;
import com.example.naejango.domain.transaction.dto.request.ModifyTransactionCommandDto;
import com.example.naejango.domain.transaction.dto.response.CreateTransactionResponseDto;
import com.example.naejango.domain.transaction.dto.response.FindTransactionDataResponseDto;
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

import javax.persistence.EntityManager;
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
    private final EntityManager em;
    private final ApplicationEventPublisher eventPublisher;
    private final WebSocketService webSocketService;
    private final ChannelService channelService;
    private final MessageService messageService;

    /** 거래 내역 조회 */
    public List<FindTransactionResponseDto> findTransactionList(Long userId){
        List<Transaction> transactionList = transactionRepository.findByUserIdOrTraderId(userId, userId);

        List<FindTransactionResponseDto> findTransactionResponseDtoList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            findTransactionResponseDtoList.add(new FindTransactionResponseDto(transaction, userId));
        }

        return findTransactionResponseDtoList;
    }

    /** 특정 거래의 정보 조회 */
    public FindTransactionResponseDto findTransactionById(Long userId, Long transactionId){
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        validateFindTransaction(userId, transaction);

        return new FindTransactionResponseDto(transaction, userId);
    }

    /** 상대 유저의 ID 요청 받아서 둘 사이의 완료 되지 않은 거래가 있다면 거래 Id와 해당 정보 return */
    public List<FindTransactionDataResponseDto> findTransactionByTraderId(Long userId, Long traderId){
        List<Transaction> transactionList = transactionRepository.findByUserIdAndTraderId(userId, traderId);

        List<FindTransactionDataResponseDto> findTransactionDataResponseDtoList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            findTransactionDataResponseDtoList.add(new FindTransactionDataResponseDto(transaction));
        }

        return findTransactionDataResponseDtoList;
    }

    /** 거래 예약 등록 */
    @Transactional
    public CreateTransactionResponseDto createTransaction(Long userId, CreateTransactionCommandDto createTransactionCommandDto){
        Item item = itemRepository.findById(createTransactionCommandDto.getItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // Validation
        validateCreateTransaction(userId, item);

        User trader = userRepository.findById(createTransactionCommandDto.getTraderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 거래 예약 상태로 생성
        Transaction transaction = createTransactionCommandDto.toEntity(em.getReference(User.class, userId), trader, item);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // trader에게 거래 알림 발송
        eventPublisher.publishEvent(new NotificationPublishDto(trader.getId(), NotificationType.TRANSACTION, "거래 요청 알림", "/api/transaction/"+savedTransaction.getId()));

        // Channel 로드 (없는 경우 생성)
        CreateChannelDto findResult = channelService.createPrivateChannel(userId, createTransactionCommandDto.getTraderId());
        Long channelId = findResult.getChannelId();

        // 거래 예약 메세지 생성
        MessagePublishCommandDto commandDto = new MessagePublishCommandDto(MessageType.TRADE, userId, channelId);

        // 메세지 발송 및 저장
        webSocketService.publishMessage(commandDto);
        messageService.publishMessage(commandDto);

        return new CreateTransactionResponseDto(savedTransaction);
    }

    /** 송금 완료로 수정 */
    @Transactional
    public void remitTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Validation
        validationRemitTransaction(userId, transaction);

        // 각 계좌 금액 교환
        Account userAccount = accountRepository.findByUserId(transaction.getUser().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account traderAccount = accountRepository.findByUserId(transaction.getTrader().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        userAccount.chargeBalance(transaction.getAmount());
        traderAccount.deductBalance(transaction.getAmount());

        // 거래 상태 "송금 완료"로 변경
        transaction.remitTransaction();
    }

    /** 거래 완료로 수정 */
    @Transactional
    public void completeTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Validation
        validateCompleteTransaction(userId, transaction);

        // 거래 상태 "거래 완료"로 변경
        transaction.completeTransaction();

        // 해당 아이템 status (거래 상태) false로 변경
        itemRepository.updateItemStatusToFalse(transaction.getItem().getId());
    }

    /** 거래 정보 수정(거래 일시, 거래 금액) 거래 상태가 "거래 예약" 일때만 가능 */
    @Transactional
    public ModifyTransactionResponseDto modifyTransaction(Long userId, Long transactionId, ModifyTransactionCommandDto modifyTransactionCommandDto) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Validation
        validateModifyTransaction(userId, transaction);

        // 거래 정보 수정
        modifyTransactionCommandDto.toEntity(transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new ModifyTransactionResponseDto(savedTransaction);
    }

    /** 거래 삭제 */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Validation
        validateDeleteTransaction(userId, transaction);

        // 거래 삭제
        transactionRepository.delete(transaction);
    }

    private void validateFindTransaction(Long userId, Transaction transaction) {
        // 해당 거래의 정보를 요청한 사람이 거래의 판매자 혹은 구매자가 아니면 예외처리
        if (!(transaction.getUser().getId().equals(userId) || transaction.getTrader().getId().equals(userId))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_READ_REQUEST);
        }
    }

    private void validateCreateTransaction(Long userId, Item item) {
        // 요청 보낸 유저가 아이템을 등록한 유저가 아니라면 "거래 등록 권한 없음" 예외처리
        if (!item.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_PERMISSION);
        }
        // 아이템 status가 "거래 진행중"이 아니거나, 타입이 "개인 판매" 가 아니라면 예외처리
        if (item.getStatus() != Boolean.TRUE || item.getItemType() != ItemType.INDIVIDUAL_SELL) {
            throw new CustomException(ErrorCode.TRANSACTION_BAD_REQUEST);
        }
    }

    private void validationRemitTransaction(Long userId, Transaction transaction) {
        // 거래의 구매자인 유저만 송금 요청을 할 수 있다.
        if (!Objects.equals(transaction.getTrader().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        // 거래의 상태가 "거래 약속"이 아니면 잘못 된 요청 예외 처리
        if (transaction.getStatus() != TransactionStatus.TRANSACTION_APPOINTMENT) {
            throw new CustomException(ErrorCode.TRANSACTION_BAD_REQUEST);
        }
    }

    private void validateCompleteTransaction(Long userId, Transaction transaction) {
        // 거래를 등록한 유저. 즉, 판매자인 유저만 "거래 완료"로 수정 할 수 있다.
        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        // 거래의 상태가 "송금 완료"가 아니라면 "거래 완료로 변경 불가
        if (transaction.getStatus() != TransactionStatus.REMITTANCE_COMPLETION) {
            throw new CustomException(ErrorCode.TRANSACTION_BAD_REQUEST);
        }
    }

    private void validateModifyTransaction(Long userId, Transaction transaction) {
        // 요청 보낸 유저와 거래를 등록한 유저가 같은지 확인
        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        // 거래 상태가 "거래 예약" 인지 체크
        if (transaction.getStatus() != TransactionStatus.TRANSACTION_APPOINTMENT) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_MODIFICATION);
        }
    }

    private void validateDeleteTransaction(Long userId, Transaction transaction) {
        // 거래 등록한 유저만 삭제 가능
        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        // 거래 상태가 예약 인지 체크
        if (!transaction.getStatus().equals(TransactionStatus.TRANSACTION_APPOINTMENT)) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_DELETE);
        }
    }
}
