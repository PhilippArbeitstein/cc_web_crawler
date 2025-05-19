package fetch;

import exceptions.PageLoadException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsoupPageLoaderTest {

    @Test
    void loadPageReturnsHtmlDocument() throws Exception {
        String url = "https://example.com";
        Document mockDoc = mock(Document.class);
        Connection mockConn = mock(Connection.class);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConn);
            when(mockConn.userAgent(anyString())).thenReturn(mockConn);
            when(mockConn.get()).thenReturn(mockDoc);

            JsoupPageLoader loader = new JsoupPageLoader();
            HtmlDocument result = loader.loadPage(url);

            assertTrue(result instanceof JsoupHtmlDocument);
        }
    }

    @Test
    void loadPageThrowsPageLoadExceptionOnError() throws Exception {
        String url = "https://fail.com";
        Connection mockConn = mock(Connection.class);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConn);
            when(mockConn.userAgent(anyString())).thenReturn(mockConn);
            when(mockConn.get()).thenThrow(new IOException("fail"));

            JsoupPageLoader loader = new JsoupPageLoader();

            assertThrows(PageLoadException.class, () -> loader.loadPage(url));
        }
    }
}
