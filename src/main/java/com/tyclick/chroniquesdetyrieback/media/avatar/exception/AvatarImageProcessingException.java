package com.tyclick.chroniquesdetyrieback.media.avatar.exception;

public class AvatarImageProcessingException extends RuntimeException {

    public AvatarImageProcessingException(Throwable cause) {
        super("Avatar image processing failed", cause);
    }
}
