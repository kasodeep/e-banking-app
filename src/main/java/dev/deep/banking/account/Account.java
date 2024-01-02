package dev.deep.banking.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    private Integer userId;

    private BigDecimal accountBalance;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private Tier tierLevel;

    private String transactionPin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Account(Integer userId) {
        this.setUserId(userId);
        this.createdAt = LocalDateTime.parse(DATE_TIME_FORMATTER.format(LocalDateTime.now()), DATE_TIME_FORMATTER);
        this.updatedAt = createdAt;
        this.setAccountBalance(new BigDecimal(0));
    }

    public Account() {
        this.createdAt = LocalDateTime.parse(DATE_TIME_FORMATTER.format(LocalDateTime.now()), DATE_TIME_FORMATTER);
        this.updatedAt = createdAt;
        this.setAccountBalance(new BigDecimal(0));
    }

    public Account(Integer userId, BigDecimal accountBalance, AccountStatus accountStatus, String accountNumber, Tier tierLevel, String transactionPin) {
        this.userId = userId;
        this.accountBalance = accountBalance;
        this.accountStatus = accountStatus;
        this.accountNumber = accountNumber;
        this.tierLevel = tierLevel;
        this.transactionPin = transactionPin;
        this.createdAt = LocalDateTime.parse(DATE_TIME_FORMATTER.format(LocalDateTime.now()), DATE_TIME_FORMATTER);
        this.updatedAt = createdAt;
    }
}
