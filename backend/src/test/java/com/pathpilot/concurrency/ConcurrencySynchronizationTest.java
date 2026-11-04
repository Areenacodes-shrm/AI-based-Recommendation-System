package com.pathpilot.concurrency;

import com.pathpilot.model.FeedbackAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading and Shared-State Synchronization Test")
class ConcurrencySynchronizationTest {

    @Test
    @DisplayName("Should prevent race conditions in concurrent feedback telemetry aggregation")
    void shouldPreventRaceConditionsInTelemetryAggregator() throws InterruptedException {
        FeedbackTelemetryAggregator aggregator = new FeedbackTelemetryAggregator();
        Long opportunityId = 42L;

        int numThreads = 10;
        int operationsPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            pool.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start simultaneously
                    for (int j = 0; j < operationsPerThread; j++) {
                        aggregator.recordInteraction(opportunityId, FeedbackAction.SAVED, null);
                        aggregator.recordInteraction(opportunityId, FeedbackAction.RATED, 5);
                        aggregator.incrementView(opportunityId);
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        boolean completedInTime = finishLatch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(completedInTime, "Concurrent threads should complete within 5 seconds");

        // Validate atomic consistency
        Map<Long, Map<String, Object>> snapshot = aggregator.getSnapshot();
        Map<String, Object> stats = snapshot.get(opportunityId);

        assertNotNull(stats, "Stats snapshot should contain opportunity record");
        assertEquals(numThreads * operationsPerThread, stats.get("saves"),
                "Saves count should match exactly without race condition loss");
        assertEquals(numThreads * operationsPerThread, stats.get("views"),
                "Views count should match exactly");
        assertEquals(numThreads * operationsPerThread, stats.get("ratings"),
                "Ratings count should match exactly");
        assertEquals(5.0, stats.get("averageRating"),
                "Average rating should strictly be 5.0");
    }

    @Test
    @DisplayName("Should execute parallel tasks and combine results deterministically")
    void shouldExecuteParallelTasksDeterministically() {
        RecommendationTaskExecutor executor = new RecommendationTaskExecutor(4, 2000L);

        List<Supplier<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int id = i;
            tasks.add(() -> {
                // Simulate compute
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                return "TaskResult-" + id;
            });
        }

        List<String> results = executor.executeParallelTasks(tasks);
        assertEquals(20, results.size(), "All 20 parallel tasks should resolve successfully");
        for (int i = 0; i < 20; i++) {
            assertTrue(results.contains("TaskResult-" + i));
        }

        executor.shutdown();
    }
}
