package com.example.naejango.domain.transaction.dto.response;

import com.example.naejango.domain.transaction.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionResponseDto {
    private Long id;

    private String date;

    private int amount;

    private String status;

    private Long userId;

    private Long traderId;

    private Long itemId;

    public CreateTransactionResponseDto(Transaction transaction) {
        this.id = transaction.getId();
        this.date = transaction.getDate().toString();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus().toString() ;
        this.userId = transaction.getUser().getId();
        this.traderId = transaction.getTrader().getId();
        this.itemId = transaction.getItem().getId();
    }
}
