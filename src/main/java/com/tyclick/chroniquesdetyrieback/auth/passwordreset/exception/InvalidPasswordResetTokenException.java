package com.tyclick.chroniquesdetyrieback.auth.passwordreset.exception;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;

public class InvalidPasswordResetTokenException extends BusinessException {

    public InvalidPasswordResetTokenException() {
        super("Password reset token is invalid or expired");
    }
}