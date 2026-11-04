package com.pathpilot.concurrency;

import com.pathpilot.model.FeedbackAction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-Safe In-Memory Telemetry and Feedback Aggregator.
 * Demonstrates Concurrency, Atomic variables, Concurrent Collections,
 * and explicit ReentrantReadWriteLock synchronization for consistent snapshotting.
 */
@Component
public class FeedbackTelemetryAggregator {

    // Internal container for thread-safe metric tracking per opportunity
    public static class OpportunityStats {
        private final AtomicInteger viewCount = new AtomicInteger(0);
        private final AtomicInteger saveCount = new AtomicInteger(0);
        private final AtomicInteger dismissCount = new AtomicInteger(0);
        private final AtomicInteger completionCount = new AtomicInteger(0);
        private final DoubleAdder totalRatingSum = new DoubleAdder();
        private final AtomicInteger ratingCount = new AtomicInteger(0);

        public void recordAction(FeedbackAction action, Integer rating) {
            switch (action) {
                case SAVED -> saveCount.incrementAndGet();
                case DISMISSED -> dismissCount.incrementAndGet();
                case COMPLETED -> completionCount.incrementAndGet();
                case RATED -> {
                    if (rating != null && rating > 0) {
                        totalRatingSum.add(rating);
                        ratingCount.incrementAndGet();
                    }
                }
            }
        }

        public int getViewCount() { return viewCount.get(); }
        public int getSaveCount() { return saveCount.get(); }
        public int getDismissCount() { return dismissCount.get(); }
        public int getCompletionCount() { return completionCount.get(); }
        public int getRatingCount() { return ratingCount.get(); }
        public double getAverageRating() {
            int count = ratingCount.get();
            return count > 0 ? (totalRatingSum.sum() / count) : 0.0;
        }
    }

    // Concurrent map storing statistics per opportunity ID
    private final ConcurrentHashMap<Long, OpportunityStats> statsMap = new ConcurrentHashMap<>();

    // ReadWriteLock to synchronize global snapshot generation and batch resets
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    /**
     * Record an interaction event concurrently without blocking read operations.
     */
    public void recordInteraction(Long opportunityId, FeedbackAction action, Integer rating) {
        if (opportunityId == null || action == null) return;

        readLock.lock();
        try {
            statsMap.computeIfAbsent(opportunityId, k -> new OpportunityStats())
                    .recordAction(action, rating);
        } finally {
            readLock.unlock();
        }
    }

    public void incrementView(Long opportunityId) {
        if (opportunityId == null) return;
        readLock.lock();
        try {
            statsMap.computeIfAbsent(opportunityId, k -> new OpportunityStats())
                    .viewCount.incrementAndGet();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Compute a real-time weight adjustment factor based on live community feedback.
     */
    public double getFeedbackAdjustmentMultiplier(Long opportunityId) {
        OpportunityStats stats = statsMap.get(opportunityId);
        if (stats == null) return 1.0;

        int saves = stats.getSaveCount();
        int dismisses = stats.getDismissCount();
        int completions = stats.getCompletionCount();

        // Ratio of positive endorsements to dismissals
        int positiveSignals = (saves * 2) + (completions * 3);
        int negativeSignals = dismisses * 2;

        if (negativeSignals > positiveSignals) {
            return Math.max(0.65, 1.0 - ((negativeSignals - positiveSignals) * 0.05));
        } else if (positiveSignals > 0) {
            return Math.min(1.25, 1.0 + (positiveSignals * 0.03));
        }
        return 1.0;
    }

    /**
     * Creates an immutable, thread-safe snapshot of all telemetry statistics.
     */
    public Map<Long, Map<String, Object>> getSnapshot() {
        writeLock.lock(); // Acquire exclusive write lock to prevent mutations during snapshot creation
        try {
            Map<Long, Map<String, Object>> snapshot = new HashMap<>();
            for (Map.Entry<Long, OpportunityStats> entry : statsMap.entrySet()) {
                OpportunityStats s = entry.getValue();
                Map<String, Object> data = new HashMap<>();
                data.put("views", s.getViewCount());
                data.put("saves", s.getSaveCount());
                data.put("dismisses", s.getDismissCount());
                data.put("completions", s.getCompletionCount());
                data.put("ratings", s.getRatingCount());
                data.put("averageRating", s.getAverageRating());
                snapshot.put(entry.getKey(), Collections.unmodifiableMap(data));
            }
            return Collections.unmodifiableMap(snapshot);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Reset telemetry (used in testing or maintenance).
     */
    public void reset() {
        writeLock.lock();
        try {
            statsMap.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
