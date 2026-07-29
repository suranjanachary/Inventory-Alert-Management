package com.inventory.alert.exception;

/**
 * Public registration must not create ADMIN accounts.
 */
public class InvalidRegistrationRoleException extends BusinessException {

    public InvalidRegistrationRoleException() {
        super("Public registration allows MANAGER or VIEWER only. ADMIN must be provisioned separately.");
    }
}
