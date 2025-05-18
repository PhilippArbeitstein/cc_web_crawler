package core;

import fetch.PageLoader;
import model.CrawlResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class CrawlPageAnalyzer {
    private final PageLoader fetcher;
    private final int maxHeadingLevel = 6;
    private final Predicate<String> isNotEmpty = text -> !text.isEmpty();
    private final Function<Element, String> extractText = element -> element.text().trim();

    public CrawlPageAnalyzer(PageLoader fetcher) {
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
            System.err.println("Failed to load or process page: " + url + " - " + e.getMessage());
            page.isFetchFailed = true;
        }

        return page;
    }

    private List<String> extractFormattedHeadings(Document document) {
        List<String> headings = new ArrayList<>();
        for (int headingLevel = 1; headingLevel <= maxHeadingLevel; headingLevel++) {
            headings.addAll(extractHeadingsAtLevel(document, headingLevel));
        }
        return headings;
    }

    private List<String> extractHeadingsAtLevel(Document document, int level) {
        Function<String, String> prefixWithHeadingLevel = text -> "h" + level + ":" + text;

        return document.select("h" + level).stream()
                .map(extractText)
                .filter(isNotEmpty)
                .map(prefixWithHeadingLevel)
                .toList();
    }

    private List<String> extractValidLinks(Document document) {
        return document.select("a[href]").stream()
                .map(this::normalizeHref)
                .filter(isNotEmpty)
                .toList();
    }

    private String normalizeHref(Element anchor) {
        return anchor.attr("abs:href").trim().toLowerCase();
    }
}