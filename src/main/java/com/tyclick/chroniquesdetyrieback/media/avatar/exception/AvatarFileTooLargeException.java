package com.tyclick.chroniquesdetyrieback.media.avatar.exception;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;

public class AvatarFileTooLargeException extends BusinessException {
    public AvatarFileTooLargeException() {
        super("Avatar file exceeds the maximum allowed size");
    }
}
