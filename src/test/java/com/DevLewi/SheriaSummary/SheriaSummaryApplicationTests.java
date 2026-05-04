package com.DevLewi.SheriaSummary;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.ai.openai.base-url=https://api.groq.com/openai",
        "DB_URL=jdbc:postgresql://localhost/test",
        "DB_USERNAME=test",
        "DB_PASSWORD=test",
        "GROQ_API_KEY=test"
})
class SheriaSummaryApplicationTests {

    @Test
    void contextLoads() {
    }
}
