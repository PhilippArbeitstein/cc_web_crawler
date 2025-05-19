package fetch;

import java.util.List;

public interface HtmlDocument {
    List<String> getLinks();
    String select(String cssQuery);
}