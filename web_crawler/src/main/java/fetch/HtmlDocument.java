package fetch;

public interface HtmlDocument {
    String getTitle();
    String getText();
    List<String> getLinks();
    Elements select(String cssQuery);
}