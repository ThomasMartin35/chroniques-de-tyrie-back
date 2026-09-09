package com.tyclick.chroniquesdetyrieback;

import com.tyclick.chroniquesdetyrieback.config.PostgresTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(PostgresTestContainerConfiguration.class)
@ActiveProfiles("test")
class ChroniquesDeTyrieBackApplicationTests {

    @Test
    void contextLoads() {
    }

}
