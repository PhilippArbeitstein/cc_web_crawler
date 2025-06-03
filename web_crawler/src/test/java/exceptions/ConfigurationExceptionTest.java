package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationExceptionTest {

    @Test
    void constructorStoresMessage() {
        ConfigurationException exception = new ConfigurationException("invalid config");
        assertEquals("invalid config", exception.getMessage());
    }
}
