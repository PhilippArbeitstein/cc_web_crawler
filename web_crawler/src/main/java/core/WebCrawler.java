package core;

import config.CrawlConfiguration;
import model.CrawlResult;
import org.slf4j.Logger;
import util.CrawlLogger;
import util.WebCrawlerUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class WebCrawler {
    private static final Logger logger = CrawlLogger.getLogger(WebCrawler.class);
    private final CrawlConfiguration config;
    private final Set<String> visitedPages;
    private final List<CrawlResult> resultsList;
    private final CrawlPageAnalyzer crawlPageAnalyzer;
    private final CrawlTaskExecutor crawlTaskExecutor;

    public WebCrawler(CrawlConfiguration config, CrawlPageAnalyzer crawlPageAnalyzer, int threadPoolSize) {
        this.config = config;
        this.visitedPages = ConcurrentHashMap.newKeySet();
        this.resultsList = Collections.synchronizedList(new ArrayList<>());
        this.crawlPageAnalyzer = crawlPageAnalyzer;
        this.crawlTaskExecutor = new CrawlTaskExecutor(threadPoolSize);
    }

    protected List<CrawlResult> crawl() {
        submitCrawlTask(config.rootUrl().toString(), 0, config.rootUrl());
        crawlTaskExecutor.waitForAllTasksToFinish();
        crawlTaskExecutor.shutdown();
        return resultsList;
    }

    private void submitCrawlTask(String url, int depth, URL rootStartUrl) {
        crawlTaskExecutor.submitTask(() -> {
            crawlRecursively(url, depth, rootStartUrl);
            return null;
        });
    }

    private void crawlRecursively(String url, int currentDepth, URL rootStartUrl) {
        if (!canCrawl(url, currentDepth, rootStartUrl)) {
            return;
        }

        markUrlAsVisited(url);
        logCrawlingProgress(url, currentDepth);

        CrawlResult result = processPage(url, currentDepth, rootStartUrl);
        handleChildLinks(result, currentDepth + 1, rootStartUrl);
    }

    private boolean canCrawl(String url, int currentDepth, URL rootStartUrl) {
        Optional<String> normalizedUrlOpt = WebCrawlerUtils.normalizeUrl(url);
        if (normalizedUrlOpt.isEmpty()) {
            return false;
        }

        String normalizedUrl = normalizedUrlOpt.get();
        return isValidUrlForCrawl(url, currentDepth) && !handleAlreadyVisited(normalizedUrl, rootStartUrl);
    }


    private void markUrlAsVisited(String url) {
        Optional<String> normalizedUrlOpt = WebCrawlerUtils.normalizeUrl(url);
        normalizedUrlOpt.ifPresent(this::markPageAsVisited);
    }



    private CrawlResult processPage(String url, int depth, URL parentUrl) {
        return processAndStorePage(url, depth, parentUrl);
    }

    private void handleChildLinks(CrawlResult result, int nextDepth, URL rootUrl) {
        if (!shouldSkipChildLinks(result)) {
            submitChildLinks(result.childLinks, nextDepth, rootUrl);
        }
    }

    protected boolean isValidUrlForCrawl(String url, int currentDepth) {
        if (url.isEmpty()) return false;

        Optional<String> normalizedOpt = WebCrawlerUtils.normalizeUrl(url);

        Predicate<String> depthCheck = normalized ->
                config.maxDepth()
                        .map(max -> currentDepth <= max)
                        .orElse(true);

        return normalizedOpt
                .filter(normalized -> WebCrawlerUtils.isDomainAllowed(normalized, config.crawlableDomains()))
                .filter(depthCheck)
                .isPresent();
    }

    private boolean handleAlreadyVisited(String normalizedUrl, URL rootUrl) {
        synchronized (visitedPages) {
            if (!visitedPages.contains(normalizedUrl)) return false;
            addStartUrlToExistingPage(normalizedUrl, rootUrl);
            return true;
        }
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
        logger.info("Crawling at %s (depth %d)\n", url, depth);
    }

    private CrawlResult processAndStorePage(String url, int depth, URL parentUrl) {
        CrawlResult result = crawlPageAnalyzer.processPage(url, depth);
        result.parentUrls.add(parentUrl);
        resultsList.add(result);
        return result;
    }

    private boolean shouldSkipChildLinks(CrawlResult result) {
        return result.isFetchFailed || result.childLinks == null;
    }

    private void submitChildLinks(List<String> links, int nextDepth, URL rootUrl) {
        for (String link : links) {
            Optional<String> normalizedLinkOpt = WebCrawlerUtils.normalizeUrl(link);
            normalizedLinkOpt.ifPresent(normalizedLink -> submitCrawlTask(normalizedLink, nextDepth, rootUrl));
        }
    }

}

