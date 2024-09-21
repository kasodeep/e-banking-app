package dev.deep.banking.notification;


import java.math.BigDecimal;

public record CreditDebitSmsAlertTemplate(
        String senderName,
        String receiverName,
        String senderPhoneNumber,
        String receiverPhoneNumber,
        BigDecimal senderAccountBalance,
        BigDecimal receiverAccountBalance,
        BigDecimal amountTransferred
) {
}
