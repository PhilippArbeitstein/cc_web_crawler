import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {
    private final String url;
    private final int depth;
    private final ArrayList<String> domains;
    private ArrayList<String> visitedUrls;
    private final String reportPath;
    private int currentDepth;
    private ReportWriter reportWriter;

    public Crawler(String url, int depth, ArrayList<String> domains) {
        this.url = url;
        this.depth = depth;
        this.domains = domains;
        currentDepth = 1;
        visitedUrls = new ArrayList<>();
        reportPath = "report.md";
        reportWriter = new ReportWriter(reportPath);
        printCrawlerInformation();
    }

    private void printCrawlerInformation() {
        System.out.println("Crawler Information");
        System.out.println("\tURL: " + this.url);
        System.out.println("\tDepth: " + this.depth);
        System.out.println("\tDomains: " + this.domains);
    }

    public void runCrawl(String urlToCrawl) {
        if (currentDepth > depth) {
            return;  // Stop if we've reached the maximum depth
        }

        String normalizedUrl = normalizeUrl(urlToCrawl);

        if (visitedUrls.contains(normalizedUrl)) {
            return;     // Don't crawl if already visited
        }


        Document crawledDom = crawlUrl(urlToCrawl);
        if (crawledDom == null) return;

        // Mark as visited AFTER successfully crawling
        visitedUrls.add(normalizedUrl);
        reportWriter.printLinkAndDepthInformation(urlToCrawl, currentDepth, false);

        getAllHeadingsFromDom(crawledDom);

        // Only proceed to deeper links if we haven't reached max depth
        if (currentDepth < depth) {
            ArrayList<String> validLinksToCrawl = getAllValidLinksFromDom(crawledDom);

            // Increment depth for child links
            currentDepth++;
            for (String link : validLinksToCrawl) {
                runCrawl(link);
            }
            currentDepth--;
        }
    }

    private String normalizeUrl(String url) {
        // Remove trailing slash if present
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }


    private ArrayList<String> getAllValidLinksFromDom(Document crawledDom) {
        return crawledDom.select("a[href]")
                .stream()
                .map(link -> link.attr("abs:href")) // Use absolute URLs
                .filter(link -> !link.isEmpty() &&
                        domains.stream().anyMatch(domain -> link.contains(domain)))
                .filter(link -> !visitedUrls.contains(normalizeUrl(link))) // Use normalized URLs for comparison
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void getAllHeadingsFromDom(Document crawledDom) {
        for (Element heading : crawledDom.select("h1, h2, h3, h4, h5, h6")) {
            String headingText = heading.text();
            reportWriter.writeHeadingIntoReport(headingText, Integer.parseInt(heading.tagName().substring(1)), currentDepth);
        }
    }

    private Document crawlUrl(String url) {
        try {
            if (visitedUrls.contains(url)) {
                return null;
            }
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            handleBrokenLink(url);
        }
        return null;
    }

    private void handleBrokenLink(String url) {
        reportWriter.printLinkAndDepthInformation(url, currentDepth, true);
    }

    public String getUrl() {
        return url;
    }
}
