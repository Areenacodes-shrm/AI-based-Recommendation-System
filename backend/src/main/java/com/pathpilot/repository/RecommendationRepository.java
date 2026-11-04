package com.pathpilot.repository;

import com.pathpilot.model.RecommendationItem;

import java.util.List;

/**
 * Interface contract for persisting recommendation runs and explanations.
 */
public interface RecommendationRepository {
    Long saveRecommendation(Long studentId, String strategyUsed, List<RecommendationItem> items);
    List<RecommendationItem> findLatestByStudentId(Long studentId);
}
