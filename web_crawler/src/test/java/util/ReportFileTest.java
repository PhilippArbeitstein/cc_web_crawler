package util;

import org.junit.jupiter.api.Test;
import exceptions.ReportFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.*;

class ReportFileTest {

    @Test
    void appendToReportWritesContent() throws Exception {
        File tempFile = File.createTempFile("report", ".md");
        ReportFile.appendToReport(tempFile.getAbsolutePath(), "line 1");

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line = reader.readLine();
            assertEquals("line 1", line);
        }

        tempFile.delete();
    }

    @Test
    void clearFileEmptiesFile() throws Exception {
        File tempFile = File.createTempFile("report", ".md");
        ReportFile.appendToReport(tempFile.getAbsolutePath(), "line 1");
        ReportFile.clearFile(tempFile.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            assertNull(reader.readLine());
        }

        tempFile.delete();
    }

    @Test
    void appendToReportThrowsException() {
        String invalidPath = "/invalid/path/report.md";
        assertThrows(ReportFileException.class, () -> ReportFile.appendToReport(invalidPath, "data"));
    }

    @Test
    void clearFileThrowsException() {
        String invalidPath = "/invalid/path/report.md";
        assertThrows(ReportFileException.class, () -> ReportFile.clearFile(invalidPath));
    }
}
