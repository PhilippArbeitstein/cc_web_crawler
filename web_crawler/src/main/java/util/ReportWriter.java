package util;

import model.CrawlResult;
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
        ReportFileUtil.clearFile(reportPath);
    }

    public void writeReport(List<CrawlResult> pages, URL rootUrl) {
        writeReportHeader(rootUrl);
        List<CrawlResult> sortedPages = sortPagesByDepth(pages);
        writePagesToReport(sortedPages);
    }

    private void writeReportHeader(URL rootUrl) {
        writeLine(MarkdownUtil2.createHeading("Crawled Website Report", 1, 0));
        writeLine(MarkdownUtil2.createHeading("Results for: " + rootUrl, 2, 0));
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
        writeLine(MarkdownUtil2.createLinkInfo(page.pageUrl, page.currentDepth, page.isFetchFailed));
        writeLine("<br>depth:" + page.currentDepth);
    }

    private void writePageHeadings(CrawlResult page) {
        if (page.headings == null) return;
        for (String heading : page.headings) {
            writeParsedHeadingIfValid(heading, page.currentDepth);
        }
    }

    private void writeParsedHeadingIfValid(String rawHeading, int depth) {
        String parsed = MarkdownUtil2.extractHeading(rawHeading, depth);
        if (parsed != null) {
            writeLine(parsed);
        }
    }

    private void writeLine(String content) {
        ReportFileUtil.appendToReport(reportPath, content);
    }
}
