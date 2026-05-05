package com.DevLewi.SheriaSummary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SheriaSummaryApplicationTests {

    @Test
    void applicationClassInstantiates() {
        assertDoesNotThrow(SheriaSummaryApplication::new);
    }
}
