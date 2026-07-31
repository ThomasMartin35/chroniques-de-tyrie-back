package com.tyclick.chroniquesdetyrieback.auth.passwordreset.notification;

public interface PasswordResetNotificationSender {

    /**
     * Sends a password reset link to the user with the provided email
     * and token.
     *
     * @param email The email address of the user to whom the notification
     * should be sent.
     * @param rawToken The password reset token to be included in the notification.
     */
    void sendPasswordResetLink(String email, String rawToken);
}
