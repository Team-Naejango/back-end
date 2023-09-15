package com.example.naejango.domain.transaction.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDto {
    private String date;

    private int amount;

    private Long traderId;

    private Long itemId;

}
