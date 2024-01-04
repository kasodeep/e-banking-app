package dev.deep.banking.notification;

import dev.deep.banking.notification.emailNotification.EmailNotification;
import dev.deep.banking.notification.emailNotification.EmailSenderService;
import dev.deep.banking.notification.emailNotification.request.FundsAlertNotificationRequest;
import dev.deep.banking.notification.smsNotification.SmsNotification;
import dev.deep.banking.notification.smsNotification.twilio.TwilioSmsSenderService;
import dev.deep.banking.user.User;
import dev.deep.banking.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSenderService {

    private final UserService userService;

    private final EmailSenderService emailSenderService;

    private final TwilioSmsSenderService smsSenderService;

    @Async
    public void sendCreditAndDebitNotification(FundsAlertNotificationRequest request) {
        User senderUser = userService.getUserByUserId(request.senderId());
        User receiverUser = userService.getUserByUserId(request.receiverId());

        sendCreditDebitEmailAlertToCustomer(new CreditDebitEmailAlertTemplate(
                senderUser.getFullName(),
                receiverUser.getFullName(),
                senderUser.getEmailAddress(),
                receiverUser.getEmailAddress(),
                request.senderNewAccountBalance(),
                request.receiverNewAccountBalance(),
                request.amountTransferred()
        ));

        sendCreditDebitSmsAlertToCustomer(new CreditDebitSmsAlertTemplate(
                senderUser.getFullName(),
                receiverUser.getFullName(),
                senderUser.getPhoneNumber(),
                receiverUser.getPhoneNumber(),
                request.senderNewAccountBalance(),
                request.receiverNewAccountBalance(),
                request.amountTransferred()
        ));
    }

    public void sendCreditDebitEmailAlertToCustomer(CreditDebitEmailAlertTemplate template) {
        // Credit alert for receiver
        final String receiverMessage = "Money In! You have been credited USD" + template.amountTransferred() +
                " from " + template.senderName() + ". You have USD" + template.receiverAccountBalance();

        emailSenderService.sendEmail(new EmailNotification(
                template.receiverEmailAddress(),
                "CREDIT ALERT",
                receiverMessage
        ));

        // Debit alert for sender
        final String senderMessage = "Money Out! You have sent USD" + template.amountTransferred() +
                " to " + template.receiverName() + ". You have USD" + template.senderAccountBalance();

        emailSenderService.sendEmail(new EmailNotification(
                template.senderEmailAddress(),
                "DEBIT ALERT",
                senderMessage));
    }

    public void sendCreditDebitSmsAlertToCustomer(CreditDebitSmsAlertTemplate template) {
        // Credit alert for receiver
        final String senderMessage = "Money In! You have been credited USD" + template.amountTransferred() +
                " from " + template.senderName() + ". You have USD" + template.receiverAccountBalance();

        smsSenderService.sendSms(new SmsNotification(
                template.receiverPhoneNumber(),
                senderMessage
        ));

        // Debit alert for sender
        final String receiverMessage = "Money Out! You have sent USD" + template.amountTransferred() +
                " to " + template.receiverName() + ". You have USD" + template.senderAccountBalance();

        smsSenderService.sendSms(new SmsNotification(
                template.senderPhoneNumber(),
                receiverMessage
        ));
    }
}