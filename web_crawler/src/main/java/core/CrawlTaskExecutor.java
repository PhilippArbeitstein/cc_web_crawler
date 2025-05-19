package core;

import org.slf4j.Logger;
import util.CrawlLogger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */

public class CrawlTaskExecutor {
    private static final Logger logger = CrawlLogger.getLogger(CrawlTaskExecutor.class);
    private final ExecutorService executor;
    private final CompletionService<Void> completionService;
    private final AtomicInteger taskCount = new AtomicInteger(0);

    public CrawlTaskExecutor(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    public void submitTask(Callable<Void> task) {
        taskCount.incrementAndGet();
        completionService.submit(task);
    }

    public void waitForAllTasksToFinish() {
        try {
            for (int i = 0; i < taskCount.get(); i++) {
                completionService.take().get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Crawling interrupted.", e);
            throw new RuntimeException("Crawling interrupted.", e);
        } catch (ExecutionException e) {
            logger.error("Crawling failed.", e);
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.warn("Executor did not terminate in time.");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
