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

    private final JsoupPageLoader loader = new JsoupPageLoader();

    @Test
    void loadPageReturnsHtmlDocument() throws Exception {
        try (MockedStatic<Jsoup> ignored = mockJsoup("https://example.com", false)) {
            HtmlDocument result = loader.loadPage("https://example.com");
            assertTrue(result instanceof JsoupHtmlDocument);
        }
    }

    @Test
    void loadPageThrowsPageLoadExceptionOnError() throws Exception {
        try (MockedStatic<Jsoup> ignored = mockJsoup("https://fail.com", true)) {
            assertThrows(PageLoadException.class, () -> loader.loadPage("https://fail.com"));
        }
    }

    private MockedStatic<Jsoup> mockJsoup(String url, boolean throwIOException) throws IOException {
        Connection mockConn = setupMockConnection(throwIOException);
        MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class);
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConn);
        return jsoupMock;
    }

    private Connection setupMockConnection(boolean throwIOException) throws IOException {
        Connection mockConn = mock(Connection.class);
        when(mockConn.userAgent(anyString())).thenReturn(mockConn);

        if (throwIOException) {
            when(mockConn.get()).thenThrow(new IOException("fail"));
        } else {
            when(mockConn.get()).thenReturn(mock(Document.class));
        }

        return mockConn;
    }

}
