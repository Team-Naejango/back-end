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
public class ModifyTransactionResponseDto {
    private String date;

    private int amount;

    public ModifyTransactionResponseDto(Transaction transaction) {
        this.date = transaction.getDate().toString();
        this.amount = transaction.getAmount();
    }
}
