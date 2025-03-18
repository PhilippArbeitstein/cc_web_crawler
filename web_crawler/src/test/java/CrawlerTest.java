import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CrawlerTest {

    private Crawler crawler;
    private String baseUrl;
    private int maxDepth;
    private ArrayList<String> crawlableDomains;
    private final String UNCRAWLABLE_URL = "https://someurl.com";
    private final String EMPTY_URL = "";

    @BeforeEach
    void setupTestSuite() {
        baseUrl = "https://teigkeller.at";
        maxDepth = 3;
        crawlableDomains = new ArrayList<>();
        crawlableDomains.add("teigkeller.at");


        crawler = new Crawler(baseUrl, maxDepth, crawlableDomains);
    }

    @AfterEach
    void teardownTestSuite() {
        baseUrl = "";
        maxDepth = -1;
        crawlableDomains = null;
        crawler = null;
    }

    @Test
    void testGetUrl() {
        assertEquals(baseUrl, crawler.getUrl());
    }

    @Test
    void testNormalizeUrl() {
        String urlWithSlash = "https://teigkeller.at/";
        String normalizedUrlWithoutSlash = "https://teigkeller.at";
        assertEquals(normalizedUrlWithoutSlash, crawler.normalizeUrl(urlWithSlash));
    }

    @Test
    void testIsMaxDepthReached() {
        assertFalse(crawler.isMaxDepthReached());

        // Simulate depth change
        crawler.currentDepth = 4;
        assertTrue(crawler.isMaxDepthReached());
    }

    @Test
    void testHasUrlBeenVisited() {
        assertFalse(crawler.hasUrlBeenVisited(baseUrl));

        // Simulate that the url has been visited
        crawler.visitedUrls.add(baseUrl);
        assertTrue(crawler.hasUrlBeenVisited(baseUrl));
    }

    @Test
    void isCrawlable() {
        assertTrue(crawler.isCrawlable(baseUrl));
        assertFalse(crawler.isCrawlable(UNCRAWLABLE_URL));
    }

    @Test
    void testIsValidLink() {
        assertTrue(crawler.isValidLink(baseUrl));
        assertFalse(crawler.isValidLink(UNCRAWLABLE_URL));
        assertFalse(crawler.isValidLink(EMPTY_URL));
    }

    @Test
    void testGetAbsoluteUrlFromRelativeLink() {
        String expectedUrl = "https://teigkeller.at/page2";

        // Create an element with a relative URL
        Element relativeLink = Jsoup.parse("<a href='/page2'></a>", baseUrl).select("a").first();
        String result = crawler.getAbsoluteUrl(relativeLink);

        assertEquals(expectedUrl, result, "Relative URL should be converted to absolute URL");
    }

    @Test
    void testGetAbsoluteUrlFromAbsoluteLink() {
        String expectedUrl = "https://teigkeller.at/page2";

        // Create an element with an absolute URL
        Element absoluteLink = Jsoup.parse("<a href='https://teigkeller.at/page2'></a>").select("a").first();
        String result = crawler.getAbsoluteUrl(absoluteLink);

        assertEquals(expectedUrl, result, "Absolute URL should remain unchanged");
    }

    @Test
    void isLinkNotVisited() { // TODO: TESTING THE SAME THING AS IN testHasUrlBeenVisited
    }


    @Test
    void testRunCrawl() {
    }


    @Test
    void crawlUrl() {
    }

    @Test
    void handleBrokenLink() {
    }

    @Test
    void isFetchFailed() {
    }

    @Test
    void writeAllHeadingsIntoReport() {
    }

    @Test
    void processHeading() {
    }

    @Test
    void crawlChildLinks() {
    }

    @Test
    void crawlValidLinks() {
    }

    @Test
    void getAllValidLinksFromDom() {
    }
}