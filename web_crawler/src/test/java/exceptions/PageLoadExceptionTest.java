package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageLoadExceptionTest {

    @Test
    void constructorStoresMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        PageLoadException exception = new PageLoadException("failed", cause);

        assertEquals("failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
