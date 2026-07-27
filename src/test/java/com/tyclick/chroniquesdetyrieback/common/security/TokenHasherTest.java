package com.tyclick.chroniquesdetyrieback.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHasherTest {

    private final TokenHasher tokenHasher = new TokenHasher();

    @Test
    void shouldGenerateSameHashForSameToken() {
        String rawToken = "mon-token-de-test";

        String firstHash = tokenHasher.hash(rawToken);
        String secondHash = tokenHasher.hash(rawToken);

        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    void shouldGenerateDifferentHashesForDifferentTokens() {
        String firstHash = tokenHasher.hash("premier-token");
        String secondHash = tokenHasher.hash("second-token");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }
}