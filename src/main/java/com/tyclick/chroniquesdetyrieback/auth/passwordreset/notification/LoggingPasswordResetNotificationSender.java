package com.tyclick.chroniquesdetyrieback.auth.passwordreset.notification;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggingPasswordResetNotificationSender
        implements PasswordResetNotificationSender {

    @Override
    public void sendPasswordResetLink(String email, String rawToken) {
        log.info(
                "Password reset requested for email '{}'. "
                        + "Reset link: "
                        + "http://localhost:5173/"
                        + "reinitialisation-mot-de-passe?token={}",
                email,
                rawToken
        );
    }
}