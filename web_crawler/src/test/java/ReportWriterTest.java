import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    private ReportWriter reportWriter;
    private Path tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("test_report", ".md");
        tempFile.toFile().deleteOnExit();
        reportWriter = new ReportWriter(tempFile.toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testAppendToReport() throws IOException {
        String textToAppend = "This is a test line.";
        reportWriter.appendToReport(textToAppend);

        String fileContent = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContent.contains(textToAppend), "File should contain the appended text");
    }

    @Test
    void testClearFile() throws IOException {
        reportWriter.appendToReport("This is a test line.");

        String fileContentBeforeClear = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContentBeforeClear.contains("This is a test line"), "File should contain the test line before clearing");

        reportWriter.clearFile();

        String fileContentAfterClear = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContentAfterClear.isEmpty(), "File should be empty after clearing");
    }

    @Test
    void testWriteHeadingIntoReport() throws IOException {
        String headingText = "Test Heading";
        int headingLevel = 2;
        int currentDepth = 3;

        reportWriter.writeHeadingIntoReport(headingText, headingLevel, currentDepth);

        String fileContent = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContent.contains("## \t---> \tTest Heading"), "File should contain the correct heading format");
    }

    @Test
    void printLinkAndDepthInformationValidLink() throws IOException {
        String validLink = "https://example.com";
        int currentDepth = 2;

        reportWriter.printLinkAndDepthInformation(validLink, currentDepth, false);

        String fileContent = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContent.contains(validLink), "File should contain the link");
        assertTrue(fileContent.contains("link to"), "File should indicate that it is a valid link");
        assertTrue(fileContent.contains("depth:2"), "File should indicate the correct depth");
    }

    @Test
    void printLinkAndDepthInformationBrokenLink() throws IOException {
        String brokenLink = "https://brokenurl.com";
        int currentDepth = 2;

        reportWriter.printLinkAndDepthInformation(brokenLink, currentDepth, true);
        String fileContent = new String(Files.readAllBytes(tempFile));
        assertTrue(fileContent.contains(brokenLink), "File should contain the broken link");
        assertTrue(fileContent.contains("broken link"), "File should indicate that it is a broken link");
    }
}