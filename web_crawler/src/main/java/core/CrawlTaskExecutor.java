package core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */

public class CrawlTaskExecutor {
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
            logError("Crawling interrupted.", e);
            System.err.println("Crawling interrupted.");
        } catch (ExecutionException e) {
            logError("Crawling failed.", e);
            System.err.println("Crawling task failed: " + e.getCause());
        }
    }

    private void logError(String message, Exception e) {
        System.err.println(message);
        if (e != null) {
            e.printStackTrace(System.err);
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate in time.");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
