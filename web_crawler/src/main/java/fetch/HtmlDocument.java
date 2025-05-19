package fetch;

import org.jsoup.select.Elements;

import java.util.List;

public interface HtmlDocument {
    String getTitle();
    String getText();
    List<String> getLinks();
    String select(String cssQuery);
}