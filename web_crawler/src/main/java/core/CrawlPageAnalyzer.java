package core;

import exceptions.PageLoadException;
import fetch.HtmlDocument;
import fetch.PageLoader;
import model.CrawlResult;
import org.slf4j.Logger;
import util.CrawlLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class CrawlPageAnalyzer {
    private static final Logger logger = CrawlLogger.getLogger(CrawlPageAnalyzer.class);
    private final PageLoader fetcher;
    private final int MAX_HEADING_LEVEL = 6;
    private final Predicate<String> isNotEmpty = text -> !text.isEmpty();

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
            HtmlDocument document = fetcher.loadPage(url);
            page.headings = extractFormattedHeadings(document);
            page.childLinks = extractValidLinks(document);
        } catch (PageLoadException e) {
            logger.error("Failed to load or process page: " + url + " - " + e.getMessage());
            page.isFetchFailed = true;
        }

        return page;
    }

    private List<String> extractFormattedHeadings(HtmlDocument document) {
        List<String> headings = new ArrayList<>();
        for (int headingLevel = 1; headingLevel <= MAX_HEADING_LEVEL; headingLevel++) {
            headings.addAll(extractHeadingsAtLevel(document, headingLevel));
        }
        return headings;
    }

    private List<String> extractHeadingsAtLevel(HtmlDocument document, int level) {
        String rawText = document.select("h" + level);
        return List.of(rawText.split("\\r?\\n")).stream()
                .map(String::trim)
                .filter(isNotEmpty)
                .map(text -> "h" + level + ":" + text)
                .toList();

    }

    private List<String> extractValidLinks(HtmlDocument document) {
        return document.getLinks().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(isNotEmpty)
                .toList();
    }
}