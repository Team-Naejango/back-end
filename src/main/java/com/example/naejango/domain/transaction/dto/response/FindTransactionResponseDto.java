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
public class FindTransactionResponseDto {
    private Long id;

    private String date;

    private int amount;

    private String status; // 구매 or 판매

    private String traderName;

    private String itemName;

    private Long itemId;

    public FindTransactionResponseDto(Transaction transaction, Long userId) {
        this.id = transaction.getId();
        this.date = transaction.getDate().toString();
        this.amount = transaction.getAmount();
        if (transaction.getUser().getId().equals(userId)) {
            this.status = "판매";
        } else if (transaction.getTrader().getId().equals(userId)) {
            this.status = "구매";
        }
        this.traderName = transaction.getTrader().getUserProfile().getNickname();
        this.itemName = transaction.getItem().getName();
        this.itemId = transaction.getItem().getId();
    }
}
