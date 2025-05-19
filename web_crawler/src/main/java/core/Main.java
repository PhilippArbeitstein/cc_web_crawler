package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import fetch.JsoupPageLoader;
import util.ReportWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class Main {
    private static final int MIN_REQUIRED_ARGS = 3;
    private static final int MIN_DEPTH = 0;
    private static final String REPORT_FILENAME = "report.md";

    private static final int THREAD_POOL_SIZE = 20;

    public static void main(String[] args) {
        if (args.length < MIN_REQUIRED_ARGS) {
            System.out.println("Correct Usage: <StartURL> <depth> <domain1,domain2,...>");
            return;
        }

        Optional<CrawlConfiguration> config = createCrawlConfiguration(args);
        if (config.isEmpty()) return;

        List<CrawlResult> crawlResults = runCrawl(config.get());
        writeReport(crawlResults, config.get());
    }

    protected static Optional<CrawlConfiguration> createCrawlConfiguration(String[] args) {
        Optional<URL> rootUrl = extractRootUrl(args[0]);
        if (rootUrl.isEmpty()) return Optional.empty();

        int maxDepth = extractDepth(args[1]);
        if (maxDepth < MIN_DEPTH) return Optional.empty();

        Set<String> allowedDomains = extractAllowedDomains(args[2]);
        if (allowedDomains.isEmpty()) return Optional.empty();

        return Optional.of(createConfiguration(rootUrl.get(), maxDepth, allowedDomains));
    }

    private static Optional<URL> extractRootUrl(String rootUrlFromArguments) {
        if (rootUrlFromArguments == null || rootUrlFromArguments.isBlank()) {
            System.out.println("No start URL provided. Please provide at least one.");
            return Optional.empty();
        }

        String cleanedUrl = rootUrlFromArguments.trim().toLowerCase();
        if (cleanedUrl.isEmpty()) {
            System.out.println("Provided start URL is empty.");
            return Optional.empty();
        }

        try {
            return Optional.of(new URL(cleanedUrl));
        } catch (MalformedURLException e) {
            System.out.printf("Invalid root URL: %s%n", cleanedUrl);
            return Optional.empty();
        }
    }

    private static int extractDepth(String depthFromArguments) {
        try {
            int depth = Integer.parseInt(depthFromArguments);
            if (depth < MIN_DEPTH) {
                System.out.println("Depth must be >= " + MIN_DEPTH + ".");
                return -1;
            }
            return depth;
        } catch (NumberFormatException e) {
            System.out.println("Invalid depth: " + depthFromArguments);
            return -1;
        }
    }

    private static Set<String> extractAllowedDomains(String domainFromArguments) {
        if (domainFromArguments == null || domainFromArguments.isBlank()) {
            System.out.println("No domains provided.");
            return Collections.emptySet();
        }

        String[] domainArray = domainFromArguments.split(",");
        Set<String> domains = new HashSet<>();
        for (String domain : domainArray) {
            String cleaned = domain.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                domains.add(cleaned);
            }
        }

        if (domains.isEmpty()) {
            System.out.println("Please provide at least one valid domain.");
        }

        return domains;
    }

    private static CrawlConfiguration createConfiguration(URL rootUrl, int maxDepth, Set<String> allowedDomains) {
        try {
            return new CrawlConfiguration(rootUrl, maxDepth, allowedDomains);
        } catch (IllegalArgumentException e) {
            System.err.println("Error creating CrawlConfiguration:");
            System.err.println("  Root URL       : " + rootUrl);
            System.err.println("  Max Depth      : " + maxDepth);
            System.err.println("  Allowed Domains: " + allowedDomains);
            System.err.println("  Reason         : " + e.getMessage());
            throw e;
        }
    }

    protected static List<CrawlResult> runCrawl(CrawlConfiguration config) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        System.out.println("[" + timestamp + "] Starting crawl");
        System.out.println(config.toString());

        WebCrawler crawler = new WebCrawler(config, new CrawlPageAnalyzer(new JsoupPageLoader()), THREAD_POOL_SIZE);
        return crawler.crawl();
    }

    protected static void writeReport(List<CrawlResult> results, CrawlConfiguration config) {
        ReportWriter writer = new ReportWriter(REPORT_FILENAME);
        writer.writeReport(results, config.rootUrl());
        System.out.println("Report written to " + REPORT_FILENAME);
    }
}
