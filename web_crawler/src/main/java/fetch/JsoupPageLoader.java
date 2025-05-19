package fetch;

import exceptions.PageLoadException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupPageLoader implements PageLoader {
    @Override
    public HtmlDocument loadPage(String url) throws PageLoadException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();
            return new JsoupHtmlDocument(doc);
        } catch (IOException e) {
            throw new PageLoadException("Failed to load page: " + url, e);
        }
    }
}