package com.example.naejango.domain.transaction.dto.request;

import com.example.naejango.domain.transaction.domain.Transaction;
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
public class ModifyTransactionRequestDto {
    private String date;

    private int amount;

    public void toEntity(Transaction transaction){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        transaction.modifyTransaction(LocalDateTime.parse(date, formatter), amount);
    }
}
