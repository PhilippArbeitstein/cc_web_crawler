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
        Document crawledDom = crawlUrl(urlToCrawl);
        if (crawledDom == null) return;

        reportWriter.printLinkAndDepthInformation(urlToCrawl, currentDepth, false);
        getAllHeadingsFromDom(crawledDom);
        getAllValidLinksFromDom(crawledDom);

        visitedUrls.add(urlToCrawl);
    }

    private ArrayList<String> getAllValidLinksFromDom(Document crawledDom) {
        ArrayList<String> allPossibleLinks = new ArrayList<>();
        for (Element links : crawledDom.select("a")) {
            allPossibleLinks.add(links.text());
        }

        // Filter the links based on domains and return the filtered list
        return filterForLinksInDomain(allPossibleLinks);
    }

    private ArrayList<String> filterForLinksInDomain(ArrayList<String> allPossibleLinks) {
        return allPossibleLinks.stream()
                .filter(link -> domains.stream().anyMatch(link::contains))
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
