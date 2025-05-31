package core;

import exceptions.PageLoadException;
import fetch.HtmlDocument;
import fetch.PageLoader;
import model.CrawlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrawlPageAnalyzerTest {

    private PageLoader mockLoader;
    private HtmlDocument mockDoc;
    private CrawlPageAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        mockLoader = mock(PageLoader.class);
        mockDoc = mock(HtmlDocument.class);
        analyzer = new CrawlPageAnalyzer(mockLoader);
    }

    @Test
    void processPage_successfulLoad_extractsData() {
        when(mockLoader.loadPage("https://test.com")).thenReturn(mockDoc);
        when(mockDoc.select("h1")).thenReturn("Heading 1");
        when(mockDoc.select("h2")).thenReturn("");
        when(mockDoc.select("h3")).thenReturn("\nHeading 3\nAnother h3\n");
        when(mockDoc.select("h4")).thenReturn("");
        when(mockDoc.select("h5")).thenReturn("");
        when(mockDoc.select("h6")).thenReturn("");
        when(mockDoc.getLinks()).thenReturn(List.of(" https://a.com  ", "\nhttps://b.com\n"));

        CrawlResult result = analyzer.processPage("https://test.com", 1);

        assertEquals("https://test.com", result.pageUrl);
        assertEquals(1, result.currentDepth);
        assertFalse(result.isFetchFailed);
        assertTrue(result.headings.contains("h1:Heading 1"));
        assertTrue(result.headings.contains("h3:Heading 3"));
        assertTrue(result.headings.contains("h3:Another h3"));
        assertTrue(result.childLinks.contains("https://a.com"));
        assertTrue(result.childLinks.contains("https://b.com"));
    }

    @Test
    void processPage_fetchFails_setsFailureFlag() {
        when(mockLoader.loadPage("https://fail.com"))
                .thenThrow(new PageLoadException("fail", null));

        CrawlResult result = analyzer.processPage("https://fail.com", 2);

        assertEquals("https://fail.com", result.pageUrl);
        assertEquals(2, result.currentDepth);
        assertTrue(result.isFetchFailed);
        assertTrue(result.headings.isEmpty());
        assertTrue(result.childLinks.isEmpty());
    }


    @Test
    void extractFormattedHeadings_multipleLevels() {
        when(mockDoc.select("h1")).thenReturn("H1");
        when(mockDoc.select("h2")).thenReturn("H2\nSubH2");
        when(mockDoc.select("h3")).thenReturn("");
        when(mockDoc.select("h4")).thenReturn("");
        when(mockDoc.select("h5")).thenReturn("");
        when(mockDoc.select("h6")).thenReturn("");

        List<String> headings = analyzer.extractFormattedHeadings(mockDoc);

        assertTrue(headings.contains("h1:H1"));
        assertTrue(headings.contains("h2:H2"));
        assertTrue(headings.contains("h2:SubH2"));
    }

    @Test
    void extractValidLinks_filtersAndFormats() {
        when(mockDoc.getLinks()).thenReturn(List.of(" HTTP://A.COM ", "", "\n  ", "https://b.com"));

        List<String> links = analyzer.extractValidLinks(mockDoc);

        assertEquals(2, links.size());
        assertTrue(links.contains("http://a.com"));
        assertTrue(links.contains("https://b.com"));
    }
}
