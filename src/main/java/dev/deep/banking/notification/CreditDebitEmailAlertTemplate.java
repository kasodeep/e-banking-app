package dev.deep.banking.notification;

import java.math.BigDecimal;

public record CreditDebitEmailAlertTemplate(
        String senderName,
        String receiverName,
        String senderEmailAddress,
        String receiverEmailAddress,
        BigDecimal senderAccountBalance,
        BigDecimal receiverAccountBalance,
        BigDecimal amountTransferred
) {
}
