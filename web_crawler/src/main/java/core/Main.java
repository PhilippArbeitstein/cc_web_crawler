package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import fetch.JsoupPageLoader;
import util.ReportWriter;
import java.net.MalformedURLException;
import java.net.URL;
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

    public static void main(String[] args) {
        if (args.length < MIN_REQUIRED_ARGS) {
            System.out.println("Correct Usage: <StartURL> <depth> <domain1,domain2,...>");
            return;
        }

        Optional<CrawlConfiguration> config = createCrawlConfiguration(args);
        if (config.isEmpty()) return;

        List<CrawlResult> results = runCrawl(config.get());
        writeReport(results, config.get());
    }

    protected static Optional<CrawlConfiguration> createCrawlConfiguration(String[] args) {
        Optional<URL> rootUrl = parseStartUrl(args[0]);
        if (rootUrl.isEmpty()) return Optional.empty();

        int maxDepth = parseDepth(args[1]);
        if (maxDepth < MIN_DEPTH) return Optional.empty();

        Set<String> allowedDomains = parseAllowedDomains(args[2]);
        if (allowedDomains.isEmpty()) return Optional.empty();

        return Optional.of(createConfiguration(rootUrl.get(), maxDepth, allowedDomains));
    }

    private static Optional<URL> parseStartUrl(String urlArg) {
        if (urlArg == null || urlArg.isBlank()) {
            System.out.println("No start URL provided. Please provide at least one.");
            return Optional.empty();
        }

        String cleanedUrl = urlArg.trim().toLowerCase();
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

    private static int parseDepth(String depthStr) {
        try {
            int depth = Integer.parseInt(depthStr);
            if (depth < MIN_DEPTH) {
                System.out.println("Depth must be >= " + MIN_DEPTH + ".");
                return -1;
            }
            return depth;
        } catch (NumberFormatException e) {
            System.out.println("Invalid depth: " + depthStr);
            return -1;
        }
    }

    private static Set<String> parseAllowedDomains(String domainArg) {
        if (domainArg == null || domainArg.isBlank()) {
            System.out.println("No domains provided.");
            return Collections.emptySet();
        }

        String[] domainArray = domainArg.split(",");
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
            CrawlConfiguration config = new CrawlConfiguration(rootUrl, maxDepth, allowedDomains);
            System.out.println("Configuration loaded: " + config);
            return config;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid configuration: " + e.getMessage());
            throw e;
        }
    }

    protected static List<CrawlResult> runCrawl(CrawlConfiguration config) {
        System.out.println("Starting crawl from: " + config.rootUrl());

        WebCrawler crawler = new WebCrawler(config, new PageProcessor(new JsoupPageLoader()));
        return crawler.crawl();
    }

    protected static void writeReport(List<CrawlResult> results, CrawlConfiguration config) {
        ReportWriter writer = new ReportWriter(REPORT_FILENAME);
        writer.writeReport(results, config.rootUrl());
        System.out.println("Report written to " + REPORT_FILENAME);
    }
}
