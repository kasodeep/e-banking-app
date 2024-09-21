package dev.deep.banking.notification.emailNotification.request;

import java.math.BigDecimal;

public record FundsAlertNotificationRequest(
        int senderId,
        int receiverId,
        BigDecimal senderNewAccountBalance,
        BigDecimal receiverNewAccountBalance,
        BigDecimal amountTransferred
) {
}
