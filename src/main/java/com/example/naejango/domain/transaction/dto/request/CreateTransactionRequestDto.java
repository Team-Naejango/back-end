package com.example.naejango.domain.transaction.dto.request;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.transaction.domain.Transaction;
import com.example.naejango.domain.transaction.domain.TransactionStatus;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDto {
    private String date;

    private int amount;

    private Long traderId;

    private Long itemId;

    public Transaction toEntity(User user, User trader, Item item){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return Transaction.builder()
                .date(LocalDateTime.parse(date, formatter))
                .amount(amount)
                .status(TransactionStatus.APPOINTMENT)
                .user(user)
                .trader(trader)
                .item(item)
                .build();
    }
}
