import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */

public class Crawler {
    public static final String allHeadings = "h1, h2, h3, h4, h5, h6";
    protected final String url;
    protected int maxDepth;
    protected final ArrayList<String> crawlableDomains;
    protected final ArrayList<String> visitedUrls;
    protected int currentDepth;
    protected final ReportWriter reportWriter;

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

    protected String normalizeUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    protected boolean isMaxDepthReached() {
        return currentDepth >= maxDepth; // TODO: maybe back to > ? currently going one level less deep than expected
    }

    protected boolean hasUrlBeenVisited(String normalizedUrl) {
        return visitedUrls.contains(normalizedUrl);
    }

    protected Document crawlUrl(String url) {
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

    protected void handleBrokenLink(String url) {
        reportWriter.printLinkAndDepthInformation(url, currentDepth, true);
    }

    protected static boolean isFetchFailed(Document fetchedDom) {
        return fetchedDom == null;
    }

    protected void writeAllHeadingsIntoReport(Document crawledDom) {
        for (Element heading : crawledDom.select(allHeadings)) {
            processHeading(heading);
        }
    }

    protected void processHeading(Element heading) {
        String headingText = heading.text();
        int headingLevel = Integer.parseInt(heading.tagName().substring(1));
        reportWriter.writeHeadingIntoReport(headingText, headingLevel, currentDepth);
    }

    protected void crawlChildLinks(Document fetchedDom) {
        if (isMaxDepthReached() || fetchedDom == null) {
            return;
        }

        currentDepth++;
        crawlValidLinks(fetchedDom);
        currentDepth--;
    }

    protected void crawlValidLinks(Document fetchedDom) {
        ArrayList<String> validLinks = getAllValidLinksFromDom(fetchedDom);
        for (String link : validLinks) {
            runCrawl(link);
        }
    }

    protected ArrayList<String> getAllValidLinksFromDom(Document crawledDom) {
        return crawledDom.select("a[href]")
                .stream()
                .map(this::getAbsoluteUrl)
                .filter(this::isValidLink)
                .filter(link -> !hasUrlBeenVisited(normalizeUrl(link)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected String getAbsoluteUrl(Element link) {
        return link.attr("abs:href");
    }

    protected boolean isValidLink(String link) {
        return !link.isEmpty() && isCrawlable(link);
    }

    protected boolean isCrawlable(String link) {
        return crawlableDomains.stream().anyMatch(link::contains);
    }

    public String getUrl() {
        return url;
    }

}
