package fetch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupPageLoader implements PageLoader {
    @Override
    public JsoupHtmlDocument loadPage(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMillis)
                    .get();
            return new JsoupHtmlDocument(doc);
        } catch (IOException e) {
            throw new PageLoadException("Failed to load page: " + url, e);
        }
    }
}