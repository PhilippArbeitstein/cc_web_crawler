package core;

import fetch.PageLoader;
import model.CrawlResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PageProcessor {

    private final PageLoader fetcher;
    private final int htmlNumberOfHeadings = 6;

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
            page.headings = extractHeadings(document);
            page.childLinks = extractLinks(document);
        } catch (Exception e) {
            page.isFetchFailed = true;
        }

        return page;
    }

    private List<String> extractHeadings(Document document) {
        List<String> headings = new ArrayList<>();
        for (int i = 1; i <= htmlNumberOfHeadings; i++) {
            Elements headerElements = document.select("h" + i);
            for (Element header : headerElements) {
                String text = header.text().trim();
                if (!text.isEmpty()) {
                    headings.add(text);
                }
            }
        }
        return headings;
    }

    private List<String> extractLinks(Document document) {
        List<String> links = new ArrayList<>();
        Elements anchors = document.select("a[href]");
        for (Element anchor : anchors) {
            String href = anchor.attr("abs:href").trim().toLowerCase();
            if (!href.isEmpty()) {
                links.add(href);
            }
        }
        return links;
    }
}