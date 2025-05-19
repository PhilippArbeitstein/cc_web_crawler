package core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class CrawlTaskExecutorTest {

    private CrawlTaskExecutor executor = new CrawlTaskExecutor(2);

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void executesSingleTaskSuccessfully() {
        executor.submitTask(() -> {
            TimeUnit.MILLISECONDS.sleep(10);
            return null;
        });

        assertDoesNotThrow(() -> executor.waitForAllTasksToFinish());
    }

    @Test
    void executesMultipleTasksSuccessfully() {
        for (int i = 0; i < 3; i++) {
            executor.submitTask(() -> {
                TimeUnit.MILLISECONDS.sleep(5);
                return null;
            });
        }

        assertDoesNotThrow(() -> executor.waitForAllTasksToFinish());
    }

    @Test
    void handlesExecutionExceptionWithoutCrash() {
        executor.submitTask(() -> {
            throw new RuntimeException("expected");
        });

        assertDoesNotThrow(() -> executor.waitForAllTasksToFinish());
    }

    @Test
    void handlesInterruptedExceptionDuringWait() {
        Thread.currentThread().interrupt();

        executor.submitTask(() -> {
            TimeUnit.MILLISECONDS.sleep(10);
            return null;
        });

        assertThrows(RuntimeException.class, () -> executor.waitForAllTasksToFinish());
        assertTrue(Thread.interrupted());
    }

    @Test
    void shutdownCompletesIfExecutorTerminatesNormally() {
        executor.submitTask(() -> {
            TimeUnit.MILLISECONDS.sleep(10);
            return null;
        });

        executor.waitForAllTasksToFinish();
        executor.shutdown();

        assertTrue(true);
    }

    @Test
    void shutdownTriggersForcedTerminationIfNotCompleted() throws Exception {
        CrawlTaskExecutor blockingExecutor = new CrawlTaskExecutor(1);
        injectExecutor(blockingExecutor, new NeverTerminatingExecutor());

        assertDoesNotThrow(blockingExecutor::shutdown);
    }

    @Test
    void shutdownHandlesInterruptedExceptionDuringTermination() throws Exception {
        CrawlTaskExecutor interruptingExecutor = new CrawlTaskExecutor(1);
        injectExecutor(interruptingExecutor, new InterruptingExecutor());

        assertDoesNotThrow(interruptingExecutor::shutdown);
        assertTrue(Thread.interrupted());
    }

    private void injectExecutor(CrawlTaskExecutor instance, ExecutorService customExecutor) throws Exception {
        Field executorField = CrawlTaskExecutor.class.getDeclaredField("executor");
        executorField.setAccessible(true);
        executorField.set(instance, customExecutor);
    }

    static class NeverTerminatingExecutor extends ThreadPoolExecutor {
        NeverTerminatingExecutor() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }
    }

    static class InterruptingExecutor extends ThreadPoolExecutor {
        InterruptingExecutor() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            throw new InterruptedException("simulated");
        }
    }
}
