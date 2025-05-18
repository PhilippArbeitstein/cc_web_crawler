package core;

import fetch.PageLoader;
import model.CrawlResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class PageProcessor {

    private final PageLoader fetcher;
    private final int maxHeadingLevel = 6;

    public PageProcessor(PageLoader fetcher) {
        this.fetcher = fetcher;
    }

    public CrawlResult processPage(String url, int depth) {
        CrawlResult page = new CrawlResult();
        page.pageUrl = url;
        page.currentDepth = depth;
        page.headings = new ArrayList<>();
        page.childLinks = new ArrayList<>();
        page.isFetchFailed = false;

        try {
            Document document = fetcher.loadPage(url);
            page.headings = extractFormattedHeadings(document);
            page.childLinks = extractValidLinks(document);
        } catch (Exception e) {
            page.isFetchFailed = true;
        }

        return page;
    }

    private List<String> extractFormattedHeadings(Document document) {
        List<String> headings = new ArrayList<>();
        for (int level = 1; level <= maxHeadingLevel; level++) {
            headings.addAll(extractHeadingsAtLevel(document, level));
        }
        return headings;
    }

    private List<String> extractHeadingsAtLevel(Document document, int level) {
        return document.select("h" + level).stream()
                .map(this::extractText)
                .filter(this::isNotEmpty)
                .map(text -> formatHeading(level, text))
                .toList();
    }

    private String extractText(Element element) {
        return element.text().trim();
    }

    private boolean isNotEmpty(String text) {
        return !text.isEmpty();
    }

    private String formatHeading(int level, String text) {
        return "h" + level + ":" + text;
    }


    private List<String> extractValidLinks(Document document) {
        return document.select("a[href]").stream()
                .map(anchor -> anchor.attr("abs:href").trim().toLowerCase())
                .filter(href -> !href.isEmpty())
                .toList();
    }

}