package com.example.naejango.domain.transaction.dto.response;

import com.example.naejango.domain.transaction.domain.Transaction;
import com.example.naejango.domain.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindTransactionDataResponseDto {
    private Long id;

    private String date;

    private int amount;

    private TransactionStatus status;

    public FindTransactionDataResponseDto(Transaction transaction) {
        this.id = transaction.getId();
        this.date = transaction.getDate().toString();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
    }
}
