package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import util.WebCrawlerUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class WebCrawler {

    private final CrawlConfiguration config;
    private final Set<String> visitedPages;
    private final List<CrawlResult> resultsList;
    private final PageProcessor pageProcessor;
    private final ExecutorService executor;
    private final CompletionService<Void> completionService;
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);
    private final int numberOfThreadsForExecutor = 20;

    public WebCrawler(CrawlConfiguration config, PageProcessor pageProcessor) {
        this.config = config;
        this.visitedPages = ConcurrentHashMap.newKeySet();
        this.resultsList = Collections.synchronizedList(new ArrayList<>());
        this.pageProcessor = pageProcessor;
        this.executor = Executors.newFixedThreadPool(numberOfThreadsForExecutor);
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    protected List<CrawlResult> crawl() {
        submitCrawlTask(config.rootUrl().toString(), 0, config.rootUrl());

        for (int i = 0; i < submittedTaskCount.get(); i++) {
            waitForAllTasksToFinish();
        }
        shutdownExecutor();
        return resultsList;
    }

    private void submitCrawlTask(String url, int depth, URL rootStartUrl) {
        incrementTaskCount();
        submitToExecutor(url, depth, rootStartUrl);
    }

    private void incrementTaskCount() {
        submittedTaskCount.incrementAndGet();
    }

    private void submitToExecutor(String url, int depth, URL rootStartUrl) {
        completionService.submit(() -> {
            crawlRecursively(url, depth, rootStartUrl);
            return null;
        });
    }

    private void waitForAllTasksToFinish() {
        try {
            completionService.take().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logError("Crawling interrupted.", e);
            System.err.println("Crawling interrupted.");
        } catch (ExecutionException e) {
            logError("Crawling failed.", e);
            System.err.println("Crawling task failed: " + e.getCause());
        }
    }

    private void shutdownExecutor() {
        executor.shutdown();
    }

    private void logError(String message, Exception e) {
        System.err.println(message);
    }

    private void crawlRecursively(String url, int currentDepth, URL rootStartUrl) {
        String normalized = WebCrawlerUtils.normalizeUrl(url);

        if (!shouldCrawl(url, currentDepth)) return;
        if (handleAlreadyVisited(normalized, rootStartUrl)) return;

        markPageAsVisited(normalized);
        logCrawlingProgress(url, currentDepth);

        CrawlResult result = processAndStorePage(url, currentDepth, rootStartUrl);

        if (shouldSkipChildLinks(result)) return;

        submitChildLinks(result.childLinks, currentDepth + 1, rootStartUrl);

    }
    protected boolean shouldCrawl(String url, int currentDepth) {
        if (currentDepth > config.maxDepth()) return false;
        if (url.isEmpty()) return false;

        String normalized = WebCrawlerUtils.normalizeUrl(url);
        if (!WebCrawlerUtils.isDomainAllowed(normalized, config.crawlableDomains())) return false;

        return true;
    }
    private boolean handleAlreadyVisited(String normalizedUrl, URL rootUrl) {
        if (!visitedPages.contains(normalizedUrl)) return false;
        addStartUrlToExistingPage(normalizedUrl, rootUrl);
        return true;
    }
    private void addStartUrlToExistingPage(String normalizedUrl, URL rootStartUrl) {
        synchronized (resultsList) {
            for (CrawlResult result : resultsList) {
                if (normalizedUrl.equals(WebCrawlerUtils.normalizeUrl(result.pageUrl))) {
                    result.parentUrls.add(rootStartUrl);
                    break;
                }
            }
        }
    }
    private void markPageAsVisited(String normalizedUrl) {
        visitedPages.add(normalizedUrl);
    }
    private void logCrawlingProgress(String url, int depth) {
        System.out.printf("Crawling at %s (depth %d)\n", url, depth);
    }

    private CrawlResult processAndStorePage(String url, int depth, URL parentUrl) {
        CrawlResult result = pageProcessor.processPage(url, depth);
        result.parentUrls.add(parentUrl);
        resultsList.add(result);
        return result;
    }
    private boolean shouldSkipChildLinks(CrawlResult result) {
        return result.isFetchFailed || result.childLinks == null;
    }

    private void submitChildLinks(List<String> links, int nextDepth, URL rootUrl) {
        for (String link : links) {
            String normalizedLink = WebCrawlerUtils.normalizeUrl(link);
            if (!normalizedLink.isEmpty()) {
                submitCrawlTask(link, nextDepth, rootUrl);
            }
        }
    }
}

