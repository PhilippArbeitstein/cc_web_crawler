package fetch;

import org.jsoup.nodes.Document;

public interface PageLoader {
    /**
     * Loads and parses the HTML content for a given URL
     *
     * @param url the URL of the page to load
     * @return the parsed HTML as a Jsoup Document
     * @throws Exception  Exception if an error occurs during loading or parsing
     */
    Document loadPage(String url) throws Exception;
}
