import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

class CrawlerTest {
    private Crawler crawler;
    private String baseUrl;
    private int maxDepth;
    private ArrayList<String> crawlableDomains;
    private final String UNCRAWLABLE_URL = "https://someurl.com";
    private final String documentContent = """
                <html><body>
                    <a href='https://www.teigkeller.at/category/all-products'>Valid Link</a>
                    <a href='/b2b'>Relative Link</a>
                    <a href='https://teigkeller.at/invalid'>Invalid Domain</a>
                    <a href=''>Empty Link</a>
                </body></html>
                """;

    private ReportWriter getMockReportWriter() throws NoSuchFieldException, IllegalAccessException {
        ReportWriter writer = mock(ReportWriter.class);

        Field reportWriterField = crawler.getClass().getDeclaredField("reportWriter");
        reportWriterField.setAccessible(true);
        reportWriterField.set(crawler, writer);
        return writer;
    }

    @BeforeEach
    void setUp() {
        baseUrl = "https://teigkeller.at";
        maxDepth = 3;
        crawlableDomains = new ArrayList<>();
        crawlableDomains.add("teigkeller.at");

        crawler = new Crawler(baseUrl, maxDepth, crawlableDomains);
    }

    @AfterEach
    void tearDown() {
        baseUrl = "";
        maxDepth = -1;
        crawlableDomains = null;
        crawler = null;
    }

    @Test
    void getInitialBaseUrl() {
        assertEquals(baseUrl, crawler.getUrl());
    }

    @Test
    void normalizeUrlRemovesTrailingSlash() {
        String urlWithSlash = "https://teigkeller.at/";
        String normalizedUrlWithoutSlash = "https://teigkeller.at";
        assertEquals(normalizedUrlWithoutSlash, crawler.normalizeUrl(urlWithSlash));
    }

    @Test
    void maxDepthReached() {
        crawler.maxDepth = 1;
        assertTrue(crawler.isMaxDepthReached());
    }

    @Test
    void maxDepthReachedExceedingLimit() {
        crawler.currentDepth = 4;
        assertTrue(crawler.isMaxDepthReached());
    }

    @Test
    void isMaxDepthReachedWhenWithingLimit() {
        crawler.currentDepth = 2;
        assertFalse(crawler.isMaxDepthReached());
    }

    @Test
    void isMaxDepthReachedAtInitialDepth() {
        assertFalse(crawler.isMaxDepthReached());
    }

    @Test
    void ifUrlNotVisited() {
        assertFalse(crawler.hasUrlBeenVisited(baseUrl));
    }

    @Test
    void ifUrlAlreadyVisited() {
        crawler.visitedUrls.add(baseUrl);
        assertTrue(crawler.hasUrlBeenVisited(baseUrl));
    }

    @Test
    void isCrawlableForAllowedDomain() {
        assertTrue(crawler.isCrawlable(baseUrl));
    }

    @Test
    void notCrawlableForForbiddenDomain() {
        assertFalse(crawler.isCrawlable(UNCRAWLABLE_URL));
    }

    @Test
    void isValidLinkForValidUrl() {
        assertTrue(crawler.isValidLink(baseUrl));
    }

    @Test
    void isValidLinkForUncrawlableUrl() {
        assertFalse(crawler.isValidLink(UNCRAWLABLE_URL));
    }

    @Test
    void isValidLinkForEmptyUrl() {
        String EMPTY_URL = "";
        assertFalse(crawler.isValidLink(EMPTY_URL));
    }

    @Test
    void convertsRelativeLinkToAbsolute() {
        Element link = Jsoup.parse("<a href='/page2'></a>", baseUrl).select("a").first();
        assertEquals("https://teigkeller.at/page2", crawler.getAbsoluteUrl(link));
    }

    @Test
    void preserveAbsoluteUrl() {
        Element link = Jsoup.parse("<a href='https://teigkeller.at/page2'></a>").select("a").first();
        assertEquals("https://teigkeller.at/page2", crawler.getAbsoluteUrl(link));
    }

    @Test
    void reportsBrokenLinkForIOException() throws Exception {
        ReportWriter writer = getMockReportWriter();
        String brokenUrl = "https://brokenurl.com";

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            // Mock the Jsoup.connect(brokenUrl) to throw IOException when calling get()
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.get()).thenThrow(new IOException("Failed to fetch URL"));
            mockedJsoup.when(() -> Jsoup.connect(brokenUrl)).thenReturn(mockConnection);

            crawler.crawlUrl(brokenUrl);
            verify(writer).printLinkAndDepthInformation(brokenUrl, crawler.currentDepth, true);
        }
    }

    @Test
    void fetchFailedWhenDocumentIsNull() {
        assertTrue(crawler.isFetchFailed(null), "The fetch should fail when the Document is null.");
    }

    @Test
    void fetchFailedWhenDocumentIsNotNull() {
        Document fetchedDom = mock(Document.class);
        assertFalse(crawler.isFetchFailed(fetchedDom), "The fetch should be successfully when the Document is not null.");
    }

    @Test
    void processHeadingWritesH1ToReport() throws Exception {
        Element heading = mock(Element.class);
        when(heading.text()).thenReturn("Heading 1");
        when(heading.tagName()).thenReturn("h1");

        ReportWriter writer = getMockReportWriter();
        crawler.processHeading(heading);
        verify(writer).writeHeadingIntoReport("Heading 1", 1, crawler.currentDepth);
    }

    @Test
    void writeAllHeadingsIntoReport() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        Element h1 = mock(Element.class);
        Element h2 = mock(Element.class);

        when(h1.text()).thenReturn("H1");
        when(h2.text()).thenReturn("H2");
        when(h1.tagName()).thenReturn("h1");
        when(h2.tagName()).thenReturn("h2");

        Elements elements = new Elements(h1, h2);
        when(doc.select(anyString())).thenReturn(elements);

        spy.writeAllHeadingsIntoReport(doc);
        verify(spy).processHeading(h1);
        verify(spy).processHeading(h2);
    }


    @Test
    void returnsNullForAlreadyVisitedUrl() {
        Crawler spy = spy(crawler);
        doReturn(true).when(spy).hasUrlBeenVisited(baseUrl);

        Document result = spy.crawlUrl(baseUrl);
        assertNull(result, "Should return null for already visited URL");
        verify(spy).hasUrlBeenVisited(baseUrl);
    }

    @Test
    void getAllValidLinksFromDomWithUnvisitedValidLinks() {
        Document doc = Jsoup.parse(documentContent, baseUrl);
        Crawler spy = spy(crawler);
        spy.visitedUrls.add("https://www.teigkeller.at/category/all-products");

        doReturn(true).when(spy).isValidLink("https://www.teigkeller.at/category/all-products");
        doReturn(true).when(spy).isValidLink("/b2b");
        doReturn(false).when(spy).isValidLink("https://teigkeller.at/invalid");
        doReturn(false).when(spy).isValidLink("");

        ArrayList<String> validLinks = spy.getAllValidLinksFromDom(doc);

        int validLinksCount = 2;
        assertEquals(validLinksCount, validLinks.size());
        assertTrue(validLinks.contains("https://teigkeller.at/b2b"));
    }

    @Test
    void getAllValidLinksFromEmptyDocument() {
        Document doc = Jsoup.parse("<html><body></body></html>");
        ArrayList<String> validLinks = crawler.getAllValidLinksFromDom(doc);
        assertTrue(validLinks.isEmpty(), "Should return empty list for document with no links");
    }

    @Test
    void getAllValidLinksFromNullDocument() {
        Document doc = null;
        assertThrows(NullPointerException.class, () -> crawler.getAllValidLinksFromDom(doc),
                "Should throw NullPointerException when document is null");
    }

    @Test
    void runCrawlForEachValidLink() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        ArrayList<String> validLinks = new ArrayList<>();
        validLinks.add("https://teigkeller.at/b2b");
        validLinks.add("https://teigkeller.at/category/all-products");

        doReturn(validLinks).when(spy).getAllValidLinksFromDom(doc);

        // We don't want to actually run the crawl in our test, so we'll make it do nothing
        doNothing().when(spy).runCrawl(anyString());

        spy.crawlValidLinks(doc);

        verify(spy).runCrawl("https://teigkeller.at/b2b");
        verify(spy).runCrawl("https://teigkeller.at/category/all-products");
        verify(spy).getAllValidLinksFromDom(doc);
    }

    @Test
    void crawlValidLinksWhenNoLinksExist() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        doReturn(new ArrayList<>()).when(spy).getAllValidLinksFromDom(doc);

        spy.crawlValidLinks(doc);

        verify(spy, never()).runCrawl(anyString());
    }

    @Test
    void crawlValidLinksOnNullDocumentThrowsException() {
        Crawler spy = spy(crawler);

        doThrow(NullPointerException.class).when(spy).getAllValidLinksFromDom(null);
        assertThrows(NullPointerException.class, () -> spy.crawlValidLinks(null),
                "Should throw NullPointerException when document is null");
        verify(spy, never()).runCrawl(anyString());
    }

    @Test
    void crawlValidLinksAbortsWhenMaxDepthReached() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        doReturn(true).when(spy).isMaxDepthReached();

        int initialDepth = spy.currentDepth;
        spy.crawlChildLinks(doc);


        verify(spy, never()).crawlValidLinks(any(Document.class));
        assertEquals(initialDepth, spy.currentDepth, "currentDepth should remain unchanged");
        verify(spy).isMaxDepthReached();
    }

    @Test
    void crawlValidLinksAbortsWhenMaxDepthNotReached() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        doReturn(false).when(spy).isMaxDepthReached();

        doNothing().when(spy).crawlValidLinks(doc);

        int initialDepth = spy.currentDepth;

        spy.crawlChildLinks(doc);
        verify(spy).crawlValidLinks(doc);
        assertEquals(initialDepth, spy.currentDepth, "currentDepth should be restored to initial value");
        verify(spy).isMaxDepthReached();
    }

    @Test
    void crawlChildLinksIgnoresNullDocument() {
        Crawler spy = spy(crawler);
        doReturn(false).when(spy).isMaxDepthReached();

        int initialDepth = spy.currentDepth;
        spy.crawlChildLinks(null);

        verify(spy, never()).crawlValidLinks(null);
        assertEquals(initialDepth, spy.currentDepth, "currentDepth should be restored to initial value");
        verify(spy).isMaxDepthReached();
    }

    @Test
    void crawlChildLinksIncrementsAndDecrementsDepthCorrectly() {
        Document doc = mock(Document.class);
        Crawler spy = spy(crawler);

        doReturn(false).when(spy).isMaxDepthReached();

        doAnswer(invocation -> {
            assertEquals(spy.currentDepth, 2, "Depth should be incremented during crawlValidLinks");
            return null;
        }).when(spy).crawlValidLinks(doc);

        assertEquals(1, spy.currentDepth, "Initial depth should be 1");
        spy.crawlChildLinks(doc);
        assertEquals(1, spy.currentDepth, "currentDepth should be restored to 1");
    }

    @Test
    void runCrawlDoesNothingIfMaxDepthReached() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler spy = spy(crawler);

        doReturn(true).when(spy).isMaxDepthReached();
        doReturn(normalizedUrl).when(spy).normalizeUrl(testUrl);

        spy.runCrawl(testUrl);

        verify(spy).isMaxDepthReached();
        verify(spy, never()).crawlUrl(anyString());

        assertFalse(spy.visitedUrls.contains(normalizedUrl), "URL should not be added to visited URLs");
    }

    @Test
    void runCrawlSkipsIfUrlAlreadyVisited() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler spy = spy(crawler);

        doReturn(false).when(spy).isMaxDepthReached();
        doReturn(normalizedUrl).when(spy).normalizeUrl(testUrl);
        doReturn(true).when(spy).hasUrlBeenVisited(normalizedUrl);

        spy.runCrawl(testUrl);

        verify(spy).hasUrlBeenVisited(normalizedUrl);
        verify(spy, never()).crawlUrl(anyString());
    }

    @Test
    void runCrawlDoesNothingWhenFetchFails() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler spy = spy(crawler);

        doReturn(normalizedUrl).when(spy).normalizeUrl(testUrl);
        doReturn(false).when(spy).isMaxDepthReached();
        doReturn(false).when(spy).hasUrlBeenVisited(normalizedUrl);
        doReturn(null).when(spy).crawlUrl(testUrl);

        spy.runCrawl(testUrl);

        verify(spy).crawlUrl(testUrl);
        verify(spy, never()).writeAllHeadingsIntoReport(any(Document.class));
        verify(spy, never()).crawlChildLinks(any(Document.class));
    }

    @Test
    void runCrawlExecutesFullFlowWhenSuccessful() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler spy = spy(crawler);
        Document doc = Jsoup.parse(documentContent, baseUrl);

        doReturn(normalizedUrl).when(spy).normalizeUrl(testUrl);
        doReturn(false).when(spy).isMaxDepthReached();
        doReturn(false).when(spy).hasUrlBeenVisited(normalizedUrl);
        doReturn(doc).when(spy).crawlUrl(testUrl);
        doNothing().when(spy).writeAllHeadingsIntoReport(doc);
        doNothing().when(spy).crawlChildLinks(doc);

        spy.runCrawl(testUrl);

        verify(spy).crawlUrl(testUrl);
        verify(spy).writeAllHeadingsIntoReport(doc);
        verify(spy).crawlChildLinks(doc);
    }
}