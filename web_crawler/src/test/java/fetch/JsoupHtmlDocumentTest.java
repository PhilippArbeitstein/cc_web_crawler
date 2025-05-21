package fetch;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsoupHtmlDocumentTest {

    @Test
    void getLinksReturnsHrefAttributes() {
        Document mockDoc = mock(Document.class);
        Element link = mock(Element.class);
        Elements elements = new Elements(link);

        when(mockDoc.select("a[href]")).thenReturn(elements);
        when(link.attr("abs:href")).thenReturn("https://example.com");

        JsoupHtmlDocument doc = new JsoupHtmlDocument(mockDoc);
        List<String> links = doc.getLinks();

        assertEquals(List.of("https://example.com"), links);
    }

    @Test
    void selectReturnsCombinedText() {
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);

        when(mockDoc.select("h1")).thenReturn(mockElements);
        when(mockElements.text()).thenReturn("Headline");

        JsoupHtmlDocument doc = new JsoupHtmlDocument(mockDoc);
        String result = doc.select("h1");

        assertEquals("Headline", result);
    }
}
