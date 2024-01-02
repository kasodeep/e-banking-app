package dev.deep.banking.user.request;

import lombok.NonNull;

public record UserRegistrationRequest(@NonNull String fullName, @NonNull String emailAddress
        , @NonNull String password, @NonNull String phoneNumber) {
}