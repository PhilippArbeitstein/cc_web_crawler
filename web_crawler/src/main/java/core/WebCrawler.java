package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import util.WebCrawlerUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
            try {
                completionService.take().get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Crawling interrupted.");
            } catch (ExecutionException e) {
                System.err.println("Crawling task failed: " + e.getCause());
            }
        }

        executor.shutdown();
        return resultsList;
    }

    private void submitCrawlTask(String url, int depth, URL rootStartUrl) {
        submittedTaskCount.incrementAndGet();
        completionService.submit(() -> {
            crawlRecursively(url, depth, rootStartUrl);
            return null;
        });
    }

    private void crawlRecursively(String url, int currentDepth, URL rootStartUrl) {
        String normalized = WebCrawlerUtils.normalizeUrl(url);

        if (!shouldCrawl(url, currentDepth)) return;

        if (visitedPages.contains(normalized)) {
            addStartUrlToExistingPage(normalized, rootStartUrl);
            return;
        }
        visitedPages.add(normalized);

        System.out.printf("Crawling at %s (depth %d)\n", url, currentDepth);

        CrawlResult page = pageProcessor.processPage(url, currentDepth);
        page.parentUrls.add(rootStartUrl);
        resultsList.add(page);

        if (page.isFetchFailed || page.childLinks == null) return;

        for (String link : page.childLinks) {
            String normalizedLink = WebCrawlerUtils.normalizeUrl(link);
            if (!normalizedLink.isEmpty()) {
                submitCrawlTask(link, currentDepth + 1, rootStartUrl);
            }
        }
    }

    private void addStartUrlToExistingPage(String normalizedUrl, URL rootStartUrl) {
        synchronized (resultsList) {
            for (CrawlResult page : resultsList) {
                if (normalizedUrl.equals(WebCrawlerUtils.normalizeUrl(page.pageUrl))) {
                    page.parentUrls.add(rootStartUrl);
                    break;
                }
            }
        }
    }


    protected boolean shouldCrawl(String url, int currentDepth) {
        if (currentDepth > config.maxDepth()) return false;
        if (url.isEmpty()) return false;

        String normalized = WebCrawlerUtils.normalizeUrl(url);
        if (!WebCrawlerUtils.isDomainAllowed(normalized, config.crawlableDomains())) return false;

        return true;
    }
}

