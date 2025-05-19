package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import fetch.JsoupPageLoader;
import org.slf4j.Logger;
import util.CrawlLogger;
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
    private static final Logger logger = CrawlLogger.getLogger(Main.class);
    private static final int MIN_REQUIRED_ARGS = 3;
    private static final int MIN_DEPTH = 0;
    private static final String REPORT_FILENAME = "report.md";
    private static final int THREAD_POOL_SIZE = 20;

    public static void main(String[] args) {
        if (args.length < MIN_REQUIRED_ARGS) {
            logger.info("Correct Usage: <StartURL> <depth> <domain1,domain2,...>");
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

        Optional<Integer> maxDepth = extractDepth(args[1]);
        System.out.println("1) " + maxDepth.get());
        if (maxDepth.isEmpty()) return Optional.empty();
        System.out.println("2) " + maxDepth.get());

        Set<String> allowedDomains = extractAllowedDomains(args[2]);
        if (allowedDomains.isEmpty()) return Optional.empty();

        return Optional.of(createConfiguration(rootUrl.get(), maxDepth, allowedDomains));
    }

    private static Optional<URL> extractRootUrl(String rootUrlFromArguments) {
        if (rootUrlFromArguments == null || rootUrlFromArguments.isBlank()) {
            logger.error("No start URL provided. Please provide at least one.");
            return Optional.empty();
        }

        String cleanedUrl = rootUrlFromArguments.trim().toLowerCase();
        if (cleanedUrl.isEmpty()) {
            logger.error("Provided start URL is empty.");
            return Optional.empty();
        }

        try {
            return Optional.of(new URL(cleanedUrl));
        } catch (MalformedURLException e) {
            logger.error("Invalid root URL: %s%n", cleanedUrl);
            return Optional.empty();
        }
    }

    private static Optional<Integer> extractDepth(String depthFromArguments) {
        try {
            int depth = Integer.parseInt(depthFromArguments);
            if (depth < MIN_DEPTH) {
                logger.error("Depth must be >= {}.", MIN_DEPTH);
                return Optional.empty();
            }
            return Optional.of(depth);
        } catch (NumberFormatException e) {
            logger.error("Invalid depth: {}", depthFromArguments, e);
            return Optional.empty();
        }
    }

    private static Set<String> extractAllowedDomains(String domainFromArguments) {
        if (domainFromArguments == null || domainFromArguments.isBlank()) {
            logger.error("No domains provided.");
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
            logger.error("Please provide at least one valid domain.");
        }

        return domains;
    }

    private static CrawlConfiguration createConfiguration(URL rootUrl, Optional<Integer> maxDepth, Set<String> allowedDomains) {
        System.out.println("3) "+maxDepth.get());
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
