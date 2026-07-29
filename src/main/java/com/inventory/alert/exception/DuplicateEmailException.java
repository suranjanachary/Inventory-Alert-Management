package com.inventory.alert.exception;

/**
 * Registration conflict when email already exists.
 */
public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException(String email) {
        super("An account already exists with email: " + email);
    }
}
