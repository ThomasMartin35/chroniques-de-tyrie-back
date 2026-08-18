package com.tyclick.chroniquesdetyrieback.media.avatar.exception;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;

public class UnsupportedAvatarFormatException extends BusinessException {
    public UnsupportedAvatarFormatException() {
        super("Avatar image format must be JPEG, PNG, or WebP");
    }
}
