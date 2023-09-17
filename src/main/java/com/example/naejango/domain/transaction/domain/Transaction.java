package com.example.naejango.domain.transaction.domain;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id")
    private User trader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public void remitTransaction(){
        this.status = TransactionStatus.REMITTANCE_COMPLETION;
    }

    public void completeTransaction(){
        this.status = TransactionStatus.TRANSACTION_COMPLETION;
    }

    public void modifyTransaction(LocalDateTime date, int amount) {
        this.date = date;
        this.amount = amount;
    }
}
