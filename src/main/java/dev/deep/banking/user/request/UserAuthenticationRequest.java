package dev.deep.banking.user.request;

import lombok.NonNull;

public record UserAuthenticationRequest(@NonNull String emailAddress, @NonNull String password) {
}
