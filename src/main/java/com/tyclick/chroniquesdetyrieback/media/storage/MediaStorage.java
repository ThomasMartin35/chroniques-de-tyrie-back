package com.tyclick.chroniquesdetyrieback.media.storage;

import org.springframework.core.io.Resource;

public interface MediaStorage {

    String store(byte[] content, String directory, String extension);

    Resource load(String storageKey);

    void delete(String storageKey);
}
