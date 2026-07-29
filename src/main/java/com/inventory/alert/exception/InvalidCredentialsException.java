package com.inventory.alert.exception;

/**
 * Wrong email/password. Mapped to 401 — do not reveal which field failed.
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
