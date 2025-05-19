package util;

import model.CrawlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    File tempFile;
    ReportWriter writer;

    @BeforeEach
    void setup() throws Exception {
        tempFile = File.createTempFile("report", ".md");
        writer = new ReportWriter(tempFile.getAbsolutePath());
    }

    @Test
    void writeReportCreatesExpectedContent() throws Exception {
        CrawlResult page = new CrawlResult();
        page.pageUrl = "https://example.com";
        page.currentDepth = 1;
        page.headings = List.of("h1:Title", "h2:Subtitle");
        page.isFetchFailed = false;

        writer.writeReport(List.of(page), new URL("https://example.com"));

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String content = reader.lines().reduce("", (a, b) -> a + b);
            assertTrue(content.contains("Crawled Website Report"));
            assertTrue(content.contains("Results for: https://example.com"));
            assertTrue(content.contains("link to"));
            assertTrue(content.contains("depth:1"));


        }
    }

    @Test
    void writeReportWithNoHeadings() throws Exception {
        CrawlResult page = new CrawlResult();
        page.pageUrl = "https://example.com/page";
        page.currentDepth = 2;
        page.headings = List.of();
        page.isFetchFailed = false;

        writer.writeReport(List.of(page), new URL("https://example.com"));

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String content = reader.lines().reduce("", (a, b) -> a + b);
            assertTrue(content.contains("depth:2"));
            assertTrue(content.contains("#"));

        }
    }

    @Test
    void writeReportHandlesBrokenPage() throws Exception {
        CrawlResult page = new CrawlResult();
        page.pageUrl = "https://fail.com";
        page.currentDepth = 0;
        page.headings = null;
        page.isFetchFailed = true;

        writer.writeReport(List.of(page), new URL("https://example.com"));

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String content = reader.lines().reduce("", (a, b) -> a + b);
            assertTrue(content.contains("broken link"));
        }
    }
}
