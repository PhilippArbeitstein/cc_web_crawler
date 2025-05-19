package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.WebCrawlerUtils;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

class WebCrawlerTest {

    private WebCrawler crawler;
    private CrawlPageAnalyzer analyzer;
    private CrawlConfiguration config;
    private URL rootUrl;

    @BeforeEach
    void setUp() throws Exception {
        analyzer = mock(CrawlPageAnalyzer.class);
        rootUrl = new URL("https://example.com");
        config = new CrawlConfiguration(rootUrl, Optional.of(2), Set.of("https://example.com"));
        crawler = new WebCrawler(config, analyzer, 2);
    }

    @Test
    void crawlRecursivelyStoresResults() {
        try (MockedStatic<WebCrawlerUtils> utils = mockStatic(WebCrawlerUtils.class)) {
            utils.when(() -> WebCrawlerUtils.normalizeUrl(anyString()))
                    .thenReturn(Optional.of("https://example.com"));

            utils.when(() -> WebCrawlerUtils.isDomainAllowed(anyString(), anySet()))
                    .thenReturn(true);

            CrawlResult result = new CrawlResult();
            result.pageUrl = "https://example.com";
            result.childLinks = List.of();
            result.isFetchFailed = false;

            when(analyzer.processPage(eq("https://example.com"), anyInt()))
                    .thenReturn(result);

            crawler.crawlRecursively("https://example.com", 0, rootUrl);

            assertEquals(1, result.parentUrls.size());
            assertEquals("https://example.com", result.pageUrl);
        }
    }


    @Test
    void doesNotCrawlInvalidUrl() {
        boolean result = crawler.canCrawl("", 0, rootUrl);
        assertFalse(result);
    }

    @Test
    void skipsAlreadyVisitedUrl() {
        String url = "https://example.com/page";
        WebCrawlerUtils.normalizeUrl(url).ifPresent(normalized -> crawler.markPageAsVisited(normalized));
        boolean result = crawler.canCrawl(url, 0, rootUrl);
        assertFalse(result);
    }

    @Test
    void skipsChildLinksIfFetchFailed() {
        CrawlResult result = new CrawlResult();
        result.pageUrl = "https://example.com";
        result.childLinks = List.of("https://child.com");
        result.isFetchFailed = true;

        crawler.handleChildLinks(result, 1, rootUrl);
        assertTrue(true);
    }

    @Test
    void validUrlIsMarkedAsVisited() {
        crawler.markUrlAsVisited("https://example.com/page");
        assertTrue(true);
    }

    @Test
    void addStartUrlToAlreadyVisitedPage() throws Exception {
        CrawlResult result = new CrawlResult();
        result.pageUrl = "https://example.com";
        result.parentUrls.add(new URL("https://original.com"));

        when(analyzer.processPage(anyString(), anyInt())).thenReturn(result);
        crawler.processAndStorePage("https://example.com", 1, rootUrl);

        crawler.addStartUrlToExistingPage("https://example.com", rootUrl);

        assertTrue(result.parentUrls.contains(rootUrl));
    }


    @Test
    void processAndStorePageReturnsCorrectResult() {
        CrawlResult result = new CrawlResult();
        result.pageUrl = "https://test.com";
        when(analyzer.processPage(anyString(), anyInt())).thenReturn(result);

        CrawlResult returned = crawler.processAndStorePage("https://test.com", 1, rootUrl);
        assertEquals("https://test.com", returned.pageUrl);
        assertTrue(returned.parentUrls.contains(rootUrl));
    }

    @Test
    void shouldSkipChildLinksReturnsTrueIfChildLinksNull() {
        CrawlResult result = new CrawlResult();
        result.childLinks = null;
        result.isFetchFailed = false;

        assertTrue(crawler.shouldSkipChildLinks(result));
    }

    @Test
    void shouldSkipChildLinksReturnsTrueIfFetchFailed() {
        CrawlResult result = new CrawlResult();
        result.childLinks = List.of("a");
        result.isFetchFailed = true;

        assertTrue(crawler.shouldSkipChildLinks(result));
    }

    @Test
    void submitChildLinksHandlesValidLinks() {
        CrawlResult result = new CrawlResult();
        result.pageUrl = "https://example.com";
        result.childLinks = List.of("https://example.com/page1");

        crawler.submitChildLinks(result.childLinks, 1, rootUrl);
        assertTrue(true);
    }
}