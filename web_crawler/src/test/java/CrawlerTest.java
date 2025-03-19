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
    private final String EMPTY_URL = "";


    private ReportWriter getMockReportWriter() throws NoSuchFieldException, IllegalAccessException {
        ReportWriter mockReportWriter = mock(ReportWriter.class);

        Field reportWriterField = crawler.getClass().getDeclaredField("reportWriter");
        reportWriterField.setAccessible(true);
        reportWriterField.set(crawler, mockReportWriter);
        return mockReportWriter;
    }


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
    void testIsMaxDepth1Recognized() {
        crawler.maxDepth = 1;
        assertTrue(crawler.isMaxDepthReached());
    }

    @Test
    void testIsMaxDepthReached() {
        assertFalse(crawler.isMaxDepthReached());

        crawler.currentDepth = 4;
        assertTrue(crawler.isMaxDepthReached());
    }

    @Test
    void testIsMaxDepthReached2() {
        assertFalse(crawler.isMaxDepthReached());

        crawler.currentDepth = 2;
        assertFalse(crawler.isMaxDepthReached());
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
    void testHandleBrokenLink() throws Exception {
        ReportWriter mockReportWriter = getMockReportWriter();
        String brokenUrl = "https://brokenurl.com";

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            // Mock the Jsoup.connect(brokenUrl) to throw IOException when calling get()
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.get()).thenThrow(new IOException("Failed to fetch URL"));
            mockedJsoup.when(() -> Jsoup.connect(brokenUrl)).thenReturn(mockConnection);

            crawler.crawlUrl(brokenUrl);
            verify(mockReportWriter).printLinkAndDepthInformation(brokenUrl, crawler.currentDepth, true);
        }
    }

    @Test
    void testIsFetchFailed_DocumentIsNull() {
        assertTrue(crawler.isFetchFailed(null), "The fetch should fail when the Document is null.");
    }

    @Test
    void testIsFetchFailed_DocumentIsNotNull() {
        Document fetchedDom = mock(Document.class);
        assertFalse(crawler.isFetchFailed(fetchedDom), "The fetch should be successfully when the Document is not null.");
    }

    @Test
    void testProcessHeading_H1() throws Exception {
        String headingText = "Heading 1";
        Element mockHeading = mock(Element.class);
        when(mockHeading.text()).thenReturn(headingText);
        when(mockHeading.tagName()).thenReturn("h1");

        ReportWriter mockReportWriter = getMockReportWriter();

        crawler.processHeading(mockHeading);
        verify(mockReportWriter).writeHeadingIntoReport(headingText, 1, crawler.currentDepth);
    }

    @Test
    void testWriteAllHeadingsIntoReport() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler); // Create a Mock Crawler to later verify

        Element mockHeading1 = mock(Element.class);
        Element mockHeading2 = mock(Element.class);
        String headingText1 = "Heading 1";
        String headingText2 = "Heading 2";

        when(mockHeading1.text()).thenReturn(headingText1);
        when(mockHeading2.text()).thenReturn(headingText2);

        when(mockHeading1.tagName()).thenReturn("h1");
        when(mockHeading2.tagName()).thenReturn("h2");

        Elements mockHeadings = new Elements();
        mockHeadings.add(mockHeading1);
        mockHeadings.add(mockHeading2);

        when(mockDocument.select(anyString())).thenReturn(mockHeadings);
        assertNotNull(mockDocument, "Mock document should not be null");

        crawlerSpy.writeAllHeadingsIntoReport(mockDocument);

        verify(crawlerSpy).processHeading(mockHeading1);
        verify(crawlerSpy).processHeading(mockHeading2);
    }


    @Test
    void testCrawlUrl_ReturnsNullForVisitedUrl() {
        Crawler crawlerSpy = spy(crawler);
        doReturn(true).when(crawlerSpy).hasUrlBeenVisited(baseUrl);

        Document result = crawlerSpy.crawlUrl(baseUrl);
        assertNull(result, "Should return null for already visited URL");
        verify(crawlerSpy).hasUrlBeenVisited(baseUrl);
    }

    @Test
    void testAllValidLinksFromValidDocument() {
        Document mockDocument = Jsoup.parse(
                "<html><body>" +
                        "<a href='https://www.teigkeller.at/category/all-products'>Valid Link 2</a>" +
                        "<a href='/b2b'>Relative Link</a>" +
                        "<a href='https://teigkeller.at/invalid'>Invalid Domain</a>" +
                        "<a href=''>Empty Link</a>" +
                        "</body></html>",
                baseUrl
        );

        Crawler crawlerSpy = spy(crawler);

        doReturn(true).when(crawlerSpy).isValidLink("https://www.teigkeller.at/category/all-products");
        doReturn(true).when(crawlerSpy).isValidLink("/b2b");
        doReturn(false).when(crawlerSpy).isValidLink("https://teigkeller.at/invalid");
        doReturn(false).when(crawlerSpy).isValidLink("");

        crawlerSpy.visitedUrls.add("https://www.teigkeller.at/category/all-products");

        ArrayList<String> validLinks = crawlerSpy.getAllValidLinksFromDom(mockDocument);
        System.out.println(validLinks);

        assertEquals(2, validLinks.size(), "Should return only valid, unvisited links");
        assertTrue(validLinks.contains("https://teigkeller.at/b2b"), "Should contain the relative link converted to absolute");
        assertFalse(validLinks.contains("https://teigkeller.at/category/all-products"), "Should not contain already visited links");
        assertFalse(validLinks.contains("https://teigkeller.at/invalid"), "Should not contain invalid domain links");
        assertFalse(validLinks.contains(""), "Should not contain empty links");
    }

    @Test
    void testGetAllValidLinksFromEmptyDocument() {
        Document emptyDocument = Jsoup.parse("<html><body></body></html>");
        ArrayList<String> validLinks = crawler.getAllValidLinksFromDom(emptyDocument);
        assertTrue(validLinks.isEmpty(), "Should return empty list for document with no links");
    }

    @Test
    void testGetAllValidLinksFromNullDocument() {
        Document nullDocument = null;
        assertThrows(NullPointerException.class, () -> crawler.getAllValidLinksFromDom(nullDocument),
                "Should throw NullPointerException when document is null");
    }

    @Test
    void testCrawlValidLinks() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler);

        ArrayList<String> validLinks = new ArrayList<>();
        validLinks.add("https://teigkeller.at/b2b");
        validLinks.add("https://teigkeller.at/category/all-products");

        doReturn(validLinks).when(crawlerSpy).getAllValidLinksFromDom(mockDocument);

        // We don't want to actually run the crawl in our test, so we'll make it do nothing
        doNothing().when(crawlerSpy).runCrawl(anyString());

        crawlerSpy.crawlValidLinks(mockDocument);

        verify(crawlerSpy).runCrawl("https://teigkeller.at/b2b");
        verify(crawlerSpy).runCrawl("https://teigkeller.at/category/all-products");
        verify(crawlerSpy).getAllValidLinksFromDom(mockDocument);
    }

    @Test
    void testCrawlValidLinksNoValidLinks() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler);

        ArrayList<String> validLinks = new ArrayList<>();

        doReturn(validLinks).when(crawlerSpy).getAllValidLinksFromDom(mockDocument);

        crawlerSpy.crawlValidLinks(mockDocument);

        verify(crawlerSpy, never()).runCrawl(anyString());
        verify(crawlerSpy).getAllValidLinksFromDom(mockDocument);
    }

    @Test
    void testCrawlValidLinksNullDocument() {
        Crawler crawlerSpy = spy(crawler);

        doThrow(NullPointerException.class).when(crawlerSpy).getAllValidLinksFromDom(null);
        assertThrows(NullPointerException.class, () -> crawlerSpy.crawlValidLinks(null),
                "Should throw NullPointerException when document is null");
        verify(crawlerSpy, never()).runCrawl(anyString());
    }

    @Test
    void testCrawlChildLinksMaxDepthReached() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler);

        doReturn(true).when(crawlerSpy).isMaxDepthReached();

        int initialDepth = crawlerSpy.currentDepth;
        crawlerSpy.crawlChildLinks(mockDocument);


        verify(crawlerSpy, never()).crawlValidLinks(any(Document.class));
        assertEquals(initialDepth, crawlerSpy.currentDepth, "currentDepth should remain unchanged");
        verify(crawlerSpy).isMaxDepthReached();
    }

    @Test
    void testCrawlChildLinksMaxDepthNotReached() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler);

        doReturn(false).when(crawlerSpy).isMaxDepthReached();

        doNothing().when(crawlerSpy).crawlValidLinks(mockDocument);

        int initialDepth = crawlerSpy.currentDepth;

        crawlerSpy.crawlChildLinks(mockDocument);
        verify(crawlerSpy).crawlValidLinks(mockDocument);
        assertEquals(initialDepth, crawlerSpy.currentDepth, "currentDepth should be restored to initial value");
        verify(crawlerSpy).isMaxDepthReached();
    }

    @Test
    void testCrawlChildLinksWithNullDocument() {
        Crawler crawlerSpy = spy(crawler);
        doReturn(false).when(crawlerSpy).isMaxDepthReached();

        int initialDepth = crawlerSpy.currentDepth;
        crawlerSpy.crawlChildLinks(null);

        verify(crawlerSpy, never()).crawlValidLinks(null);
        assertEquals(initialDepth, crawlerSpy.currentDepth, "currentDepth should be restored to initial value");
        verify(crawlerSpy).isMaxDepthReached();
    }

    @Test
    void testCrawlChildLinksDepthIncrementAndDecrement() {
        Document mockDocument = mock(Document.class);
        Crawler crawlerSpy = spy(crawler);

        doReturn(false).when(crawlerSpy).isMaxDepthReached();

        doAnswer(invocation -> {
            assertEquals(crawlerSpy.currentDepth, 2, "Depth should be incremented during crawlValidLinks");
            return null;
        }).when(crawlerSpy).crawlValidLinks(mockDocument);

        assertEquals(1, crawlerSpy.currentDepth, "Initial depth should be 1");
        crawlerSpy.crawlChildLinks(mockDocument);
        assertEquals(1, crawlerSpy.currentDepth, "currentDepth should be restored to 1");
    }

    @Test
    void testRunCrawlMaxDepthReached() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler crawlerSpy = spy(crawler);

        doReturn(true).when(crawlerSpy).isMaxDepthReached();
        doReturn(normalizedUrl).when(crawlerSpy).normalizeUrl(testUrl);

        crawlerSpy.runCrawl(testUrl);

        verify(crawlerSpy).isMaxDepthReached();
        verify(crawlerSpy).normalizeUrl(testUrl);
        verify(crawlerSpy, never()).hasUrlBeenVisited(normalizedUrl);
        verify(crawlerSpy, never()).crawlUrl(anyString());
        verify(crawlerSpy, never()).writeAllHeadingsIntoReport(any(Document.class));
        verify(crawlerSpy, never()).crawlChildLinks(any(Document.class));
        assertFalse(crawlerSpy.visitedUrls.contains(normalizedUrl), "URL should not be added to visited URLs");
    }

    @Test
    void testRunCrawl_UrlAlreadyVisited() throws Exception {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler crawlerSpy = spy(crawler);
        ReportWriter mockReportWriter = getMockReportWriter();

        doReturn(false).when(crawlerSpy).isMaxDepthReached();
        doReturn(normalizedUrl).when(crawlerSpy).normalizeUrl(testUrl);
        doReturn(true).when(crawlerSpy).hasUrlBeenVisited(normalizedUrl);

        crawlerSpy.runCrawl(testUrl);

        verify(crawlerSpy).isMaxDepthReached();
        verify(crawlerSpy).normalizeUrl(testUrl);
        verify(crawlerSpy).hasUrlBeenVisited(normalizedUrl);
        verify(crawlerSpy, never()).crawlUrl(anyString());
        verify(mockReportWriter, never()).printLinkAndDepthInformation(anyString(), anyInt(), anyBoolean());
        verify(crawlerSpy, never()).writeAllHeadingsIntoReport(any(Document.class));
        verify(crawlerSpy, never()).crawlChildLinks(any(Document.class));
    }

    @Test
    void testRunCrawlFetchFailed() {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler crawlerSpy = spy(crawler);

        doReturn(normalizedUrl).when(crawlerSpy).normalizeUrl(testUrl);
        doReturn(false).when(crawlerSpy).isMaxDepthReached();
        doReturn(false).when(crawlerSpy).hasUrlBeenVisited(normalizedUrl);
        doReturn(null).when(crawlerSpy).crawlUrl(testUrl);

        crawlerSpy.runCrawl(testUrl);

        verify(crawlerSpy).isMaxDepthReached();
        verify(crawlerSpy).normalizeUrl(testUrl);
        verify(crawlerSpy).hasUrlBeenVisited(normalizedUrl);
        verify(crawlerSpy).crawlUrl(testUrl);
        verify(crawlerSpy, never()).writeAllHeadingsIntoReport(any(Document.class));
        verify(crawlerSpy, never()).crawlChildLinks(any(Document.class));
    }

    @Test
    void testRunCrawlSuccessfulCrawl() throws Exception {
        String testUrl = "https://teigkeller.at/";
        String normalizedUrl = "https://teigkeller.at";
        Crawler crawlerSpy = spy(crawler);
        Document mockDocument = Jsoup.parse(
                "<html><body>" +
                        "<a href='https://www.teigkeller.at/category/all-products'>Valid Link 2</a>" +
                        "<a href='/b2b'>Relative Link</a>" +
                        "<a href='https://teigkeller.at/invalid'>Invalid Domain</a>" +
                        "<a href=''>Empty Link</a>" +
                        "</body></html>",
                baseUrl
        );

        doReturn(normalizedUrl).when(crawlerSpy).normalizeUrl(testUrl);
        doReturn(false).when(crawlerSpy).isMaxDepthReached();
        doReturn(false).when(crawlerSpy).hasUrlBeenVisited(normalizedUrl);
        doReturn(mockDocument).when(crawlerSpy).crawlUrl(testUrl);
        doNothing().when(crawlerSpy).writeAllHeadingsIntoReport(mockDocument);
        doNothing().when(crawlerSpy).crawlChildLinks(mockDocument);

        crawlerSpy.runCrawl(testUrl);

        verify(crawlerSpy).normalizeUrl(testUrl);
        verify(crawlerSpy).isMaxDepthReached();
        verify(crawlerSpy).hasUrlBeenVisited(normalizedUrl);
        verify(crawlerSpy).crawlUrl(testUrl);
        verify(crawlerSpy).writeAllHeadingsIntoReport(mockDocument);
        verify(crawlerSpy).crawlChildLinks(mockDocument);
    }
}