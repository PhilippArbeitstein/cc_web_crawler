import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {
    private final String url;
    private final int maxDepth;
    private final ArrayList<String> crawlableDomains;
    private final ArrayList<String> visitedUrls;
    private int currentDepth;
    private final ReportWriter reportWriter;

    public Crawler(String url, int maxDepth, ArrayList<String> crawlableDomains) {
        this.url = url;
        this.maxDepth = maxDepth;
        this.crawlableDomains = crawlableDomains;
        currentDepth = 1;
        visitedUrls = new ArrayList<>();
        String reportPath = "report.md";
        reportWriter = new ReportWriter(reportPath);
        printCrawlerInformation();
    }

    private void printCrawlerInformation() {
        System.out.println("Crawler Information");
        System.out.println("\tURL: " + this.url);
        System.out.println("\tDepth: " + this.maxDepth);
        System.out.println("\tDomains: " + this.crawlableDomains);
    }

    public void runCrawl(String urlToCrawl) {
        String normalizedUrl = normalizeUrl(urlToCrawl);
        if (isMaxDepthReached() || hasUrlBeenVisited(normalizedUrl)) return;

        Document fetchedDom = crawlUrl(urlToCrawl);
        if (isFetchFailed(fetchedDom)) return;

        visitedUrls.add(normalizedUrl);
        reportWriter.printLinkAndDepthInformation(urlToCrawl, currentDepth, false);
        writeAllHeadingsIntoReport(fetchedDom);
        crawlChildLinks(fetchedDom);
    }

    private String normalizeUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private boolean isMaxDepthReached() {
        return currentDepth > maxDepth;
    }

    private boolean hasUrlBeenVisited(String normalizedUrl) {
        return visitedUrls.contains(normalizedUrl);
    }

    private Document crawlUrl(String url) {
        try {
            if (hasUrlBeenVisited(url)) {
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

    private static boolean isFetchFailed(Document fetchedDom) {
        return fetchedDom == null;
    }

    private void writeAllHeadingsIntoReport(Document crawledDom) {
        for (Element heading : crawledDom.select("h1, h2, h3, h4, h5, h6")) {
            processHeading(heading);
        }
    }

    private void processHeading(Element heading) {
        String headingText = heading.text();
        int headingLevel = Integer.parseInt(heading.tagName().substring(1));
        reportWriter.writeHeadingIntoReport(headingText, headingLevel, currentDepth);
    }

    private void crawlChildLinks(Document fetchedDom) {
        if (isMaxDepthReached()) {
            return;
        }

        currentDepth++;
        crawlValidLinks(fetchedDom);
        currentDepth--;
    }

    private void crawlValidLinks(Document fetchedDom) {
        ArrayList<String> validLinks = getAllValidLinksFromDom(fetchedDom);
        for (String link : validLinks) {
            runCrawl(link);
        }
    }

    private ArrayList<String> getAllValidLinksFromDom(Document crawledDom) {
        return crawledDom.select("a[href]")
                .stream()
                .map(this::getAbsoluteUrl)
                .filter(this::isValidLink)
                .filter(this::isLinkNotVisited)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String getAbsoluteUrl(Element link) {
        return link.attr("abs:href");
    }

    private boolean isValidLink(String link) {
        return !link.isEmpty() && isCrawlable(link);
    }

    private boolean isCrawlable(String link) {
        return crawlableDomains.stream().anyMatch(link::contains);
    }

    private boolean isLinkNotVisited(String link) {
        return !hasUrlBeenVisited(normalizeUrl(link));
    }

    public String getUrl() {
        return url;
    }
}
