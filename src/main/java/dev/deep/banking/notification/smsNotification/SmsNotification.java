package dev.deep.banking.notification.smsNotification;

public record SmsNotification(
        String receiverPhoneNumber,
        String message
) {
}

