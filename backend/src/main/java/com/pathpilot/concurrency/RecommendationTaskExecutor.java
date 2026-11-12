package com.pathpilot.concurrency;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * High-performance concurrency executor utilizing a managed ExecutorService and
 * CompletableFuture pipeline for parallel candidate evaluation and scoring.
 */
@Component
public class RecommendationTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(RecommendationTaskExecutor.class);

    private final ExecutorService executorService;
    private final long timeoutMs;

    public RecommendationTaskExecutor(
            @Value("${pathpilot.recommendation.thread-pool-size:8}") int poolSize,
            @Value("${pathpilot.recommendation.timeout-ms:2500}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
        // Bounded thread pool with custom named thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            private final java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "pathpilot-scorer-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };

        this.executorService = new ThreadPoolExecutor(
                poolSize,
                poolSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("Initialized RecommendationTaskExecutor with pool size: {}, timeout: {}ms", poolSize, timeoutMs);
    }

    /**
     * Executes multiple scoring tasks in parallel and returns their aggregated results deterministically.
     */
    public <T> List<T> executeParallelTasks(List<Supplier<T>> taskSuppliers) {
        if (taskSuppliers == null || taskSuppliers.isEmpty()) {
            return List.of();
        }

        // Spawn parallel CompletableFutures on the managed executor
        List<CompletableFuture<T>> futures = taskSuppliers.stream()
                .map(supplier -> CompletableFuture.supplyAsync(supplier, executorService)
                        .exceptionally(ex -> {
                            log.error("Scoring task failed during parallel execution", ex);
                            return null;
                        }))
                .toList();

        // Wait for all futures to resolve within timeout limit
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allOf.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Parallel scoring timed out after {}ms. Collecting partial completed results.", timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Parallel scoring was interrupted", e);
        } catch (ExecutionException e) {
            log.error("Execution exception during parallel scoring", e);
        }

        // Collect resolved non-null results
        return futures.stream()
                .filter(f -> f.isDone() && !f.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public boolean isHealthy() {
        return !executorService.isShutdown() && !executorService.isTerminated();
    }

    public int getActiveThreadCount() {
        if (executorService instanceof ThreadPoolExecutor tpe) {
            return tpe.getActiveCount();
        }
        return 0;
    }

    public int getQueueSize() {
        if (executorService instanceof ThreadPoolExecutor tpe) {
            return tpe.getQueue().size();
        }
        return 0;
    }

    public int getCorePoolSize() {
        if (executorService instanceof ThreadPoolExecutor tpe) {
            return tpe.getCorePoolSize();
        }
        return 0;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Gracefully shutting down RecommendationTaskExecutor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
