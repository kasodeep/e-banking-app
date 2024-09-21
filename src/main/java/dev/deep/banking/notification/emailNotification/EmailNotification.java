package dev.deep.banking.notification.emailNotification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailNotification {

    private String receiverEmail;

    private String subject;

    private String message;
}