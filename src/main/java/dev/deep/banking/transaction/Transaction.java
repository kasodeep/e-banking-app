package dev.deep.banking.transaction;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String senderAccountNumber;

    @Column(nullable = false)
    private String receiverAccountNumber;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String referenceNum;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Transaction(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount, String referenceNum, String description, TransactionStatus status, String senderName, String receiverName) {
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.referenceNum = referenceNum;
        this.description = description;
        this.status = status;
        this.createdAt = LocalDateTime.parse(DATE_TIME_FORMATTER.format(LocalDateTime.now()), DATE_TIME_FORMATTER);
        this.updatedAt = createdAt;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    public Transaction() {
        this.createdAt = LocalDateTime.parse(DATE_TIME_FORMATTER.format(LocalDateTime.now()), DATE_TIME_FORMATTER);
        this.updatedAt = createdAt;
    }
}

