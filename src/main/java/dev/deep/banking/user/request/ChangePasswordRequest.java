package dev.deep.banking.user.request;


import lombok.NonNull;

public record ChangePasswordRequest(
        @NonNull String oldPassword,
        @NonNull String newPassword
) { }
