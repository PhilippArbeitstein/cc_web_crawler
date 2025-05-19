package fetch;

public class JsoupHtmlDocument implements HtmlDocument {
    private final Document document;

    public JsoupHtmlDocument(Document document) {
        this.document = document;
    }

    @Override
    public String getTitle() {
        return document.title();
    }

    @Override
    public String getText() {
        return document.body().text();
    }

    @Override
    public List<String> getLinks() {
        Elements links = document.select("a[href]");
        return links.stream()
                .map(e -> e.attr("abs:href"))
                .collect(Collectors.toList());
    }

    @Override
    public String select(String cssQuery) {
        return document.select(cssQuery).text();
    }
}