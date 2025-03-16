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

        Document crawledDom = crawlUrl(urlToCrawl);
        if (crawledDom == null) return;

        visitedUrls.add(urlToCrawl);
        reportWriter.printLinkAndDepthInformation(urlToCrawl, currentDepth, false);

        getAllHeadingsFromDom(crawledDom);

        ArrayList<String> validLinksToCrawl = getAllValidLinksFromDom(crawledDom);

        currentDepth++;
        validLinksToCrawl.forEach(link -> runCrawl(link));
        currentDepth--;
    }

    private ArrayList<String> getAllValidLinksFromDom(Document crawledDom) {
        return crawledDom.select("a[href]")
                .stream()
                .map(link -> link.attr("href"))
                .filter(link -> (link.startsWith("http://") || link.startsWith("https://"))
                        && domains.stream().anyMatch(link::contains))
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
