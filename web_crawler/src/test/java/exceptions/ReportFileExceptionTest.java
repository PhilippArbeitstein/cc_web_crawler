package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportFileExceptionTest {

    @Test
    void constructorStoresMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        ReportFileException exception = new ReportFileException("error", cause);

        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
