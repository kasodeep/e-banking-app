package dev.deep.banking.transaction.request;


import lombok.NonNull;

import java.math.BigDecimal;

public record FundsTransferRequest(
        @NonNull
        String receiverAccountNumber,
        @NonNull
        String senderAccountNumber,
        @NonNull
        BigDecimal amount,
        @NonNull
        String transactionPin,
        String narration
) {
}
