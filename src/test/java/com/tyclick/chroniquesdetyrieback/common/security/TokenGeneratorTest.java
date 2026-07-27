package com.tyclick.chroniquesdetyrieback.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGeneratorTest {

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    @Test
    void shouldGenerateDifferentTokens() {
        String firstToken = tokenGenerator.generate();
        String secondToken = tokenGenerator.generate();

        assertThat(firstToken).isNotBlank();
        assertThat(secondToken).isNotBlank();
        assertThat(firstToken).isNotEqualTo(secondToken);
    }
}