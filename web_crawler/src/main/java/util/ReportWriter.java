package util;

import exceptions.ReportFileException;
import model.CrawlResult;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class ReportWriter {
    private final String reportPath;

    public ReportWriter(String reportPath) {
        this.reportPath = reportPath;
        try {
            ReportFile.clearFile(reportPath);
        } catch (ReportFileException e) {
            System.err.println("Failed to clear report file: " + reportPath);
            e.printStackTrace();
        }    }

    public void writeReport(List<CrawlResult> pages, URL rootUrl) {
        writeReportHeader(rootUrl);
        List<CrawlResult> sortedPages = sortPagesByDepth(pages);
        writePagesToReport(sortedPages);
    }

    private void writeReportHeader(URL rootUrl) {
        writeLine(Markdown.createHeading("Crawled Website Report", 1, 0));
        writeLine(Markdown.createHeading("Results for: " + rootUrl, 2, 0));
    }

    private List<CrawlResult> sortPagesByDepth(List<CrawlResult> pages) {
        return pages.stream()
                .sorted(Comparator.comparingInt(p -> p.currentDepth))
                .toList();
    }

    private void writePagesToReport(List<CrawlResult> pages) {
        for (CrawlResult page : pages) {
            writePageLinkInfo(page);
            writePageHeadings(page);
        }
    }

    private void writePageLinkInfo(CrawlResult page) {
        writeLine(Markdown.createLinkInfo(page.pageUrl, page.currentDepth, page.isFetchFailed));
        writeLine("<br>depth:" + page.currentDepth);
    }

    private void writePageHeadings(CrawlResult page) {
        if (hasNoHeadings(page)) return;
        for (String heading : page.headings) {
            writeParsedHeadingIfValid(heading, page.currentDepth);
        }
    }

    private boolean hasNoHeadings(CrawlResult page) {
        return page.headings == null || page.headings.isEmpty();
    }

    private void writeParsedHeadingIfValid(String rawHeading, int depth) {
        Markdown.extractHeading(rawHeading, depth)
                .ifPresent(this::writeLine);

    }

    private void writeLine(String content) {
        try {
            ReportFile.appendToReport(reportPath, content);
        } catch (ReportFileException e) {
            System.err.println("Failed to append to report file: " + reportPath);
            e.printStackTrace();
        }
    }
}
