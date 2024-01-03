package dev.deep.banking.account.response;

import java.math.BigDecimal;

public record AccountOverviewResponse(BigDecimal accountBalance, String accountNumber, String TierLevel,
                                      String accountStatus) {
}