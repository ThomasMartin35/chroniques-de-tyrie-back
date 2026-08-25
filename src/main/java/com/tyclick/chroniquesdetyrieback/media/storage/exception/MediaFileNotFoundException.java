package com.tyclick.chroniquesdetyrieback.media.storage.exception;

public class MediaFileNotFoundException extends MediaStorageException {

    public MediaFileNotFoundException() {
        super("Media file does not exist or is not readable");
    }
}