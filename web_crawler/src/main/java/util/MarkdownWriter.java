package util;

import model.CrawlResult;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MarkdownWriter {

    public void write(List<CrawlResult> pages, String filename, URL rootUrl) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("# Crawled Website Report\n\n");

            writer.write(String.format("## Results for: %s\n\n", rootUrl));

            List<CrawlResult> forRoot = pages.stream()
                    .filter(p -> p.parentUrls.contains(rootUrl))
                    .sorted(Comparator.comparingInt(p -> p.currentDepth))
                    .toList();

            for (CrawlResult page : forRoot) {
                writePage(writer, page, forRoot);
                writer.write("\n");
            }
        }
    }

    private void writePage(FileWriter writer, CrawlResult page, List<CrawlResult> allPages) throws IOException {
        String indent = MarkdownUtils.indent(page.currentDepth);
        String arrow = "→".repeat(Math.max(1, page.currentDepth));

        writer.write(String.format("%s### %s %s\n", indent, arrow, page.pageUrl));
        writer.write(String.format("%s- [%s] Page %s\n", indent,
                page.isFetchFailed ? "✗" : "✓",
                page.isFetchFailed ? "could not be loaded" : "loaded successfully"));

        writeHeadings(writer, page.headings, indent);
        writeLinks(writer, page, allPages, indent);
    }

    private void writeHeadings(FileWriter writer, List<String> headings, String indent) throws IOException {
        if (headings == null || headings.isEmpty()) return;

        writer.write(String.format("%s- Headings:\n", indent));
        for (String heading : headings) {
            writer.write(String.format("%s  - %s\n", indent, heading));
        }
    }

    private void writeLinks(FileWriter writer, CrawlResult page, List<CrawlResult> allPages, String indent) throws IOException {
        Set<String> uniqueLinks = MarkdownUtils.extractUniqueLinks(page);
        if (uniqueLinks.isEmpty()) return;

        writer.write(String.format("%s- Links:\n", indent));
        for (String link : uniqueLinks) {
            boolean broken = MarkdownUtils.isLinkBroken(link, allPages);
            writer.write(String.format("%s  - [%s](%s)%s\n", indent,
                    link, link, broken ? " ❌ broken" : ""));
        }
    }
}