package com.tyclick.chroniquesdetyrieback.media.delivery.exception;

public class PublicMediaNotFoundException extends RuntimeException {

    public PublicMediaNotFoundException() {
        super("Public media not found");
    }
}