package com.tyclick.chroniquesdetyrieback.media.avatar.exception;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;

public class InvalidAvatarImageException extends BusinessException {

    public InvalidAvatarImageException() {
        super("Avatar file does not contain a valid image");
    }

    public InvalidAvatarImageException(String message) {
        super(message);
    }
}
