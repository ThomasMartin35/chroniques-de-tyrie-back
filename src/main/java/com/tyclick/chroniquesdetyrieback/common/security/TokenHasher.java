package com.tyclick.chroniquesdetyrieback.common.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenHasher {

    private static final String HASH_ALGORITHM = "SHA-256";

    public String hash(String token) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(HASH_ALGORITHM);

            byte[] hashBytes = messageDigest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "The hashing algorithm " + HASH_ALGORITHM + " is not available.",
                    exception
            );
        }
    }
}