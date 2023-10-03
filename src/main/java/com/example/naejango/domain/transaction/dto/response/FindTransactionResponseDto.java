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

    private int amount; // 판매는 거래 금액 그대로, 구매는 거래 금액의 -로 응답

    private String status; // 구매 or 판매

    private String traderName;

    private String itemName;

    private Long itemId;

    public FindTransactionResponseDto(Transaction transaction, Long userId) {
        this.id = transaction.getId();
        this.date = transaction.getDate().toString();

        if (transaction.getUser().getId().equals(userId)) {
            this.amount = transaction.getAmount();
            this.status = "판매";
            this.traderName = transaction.getTrader().getUserProfile().getNickname();
        } else if (transaction.getTrader().getId().equals(userId)) {
            this.amount = -1 * transaction.getAmount();
            this.status = "구매";
            this.traderName = transaction.getUser().getUserProfile().getNickname();
        }

        if (transaction.getItem() == null) {
            this.itemName = "삭제된 아이템";
            this.itemId = null;
        } else {
            this.itemName = transaction.getItem().getName();
            this.itemId = transaction.getItem().getId();
        }

    }
}
