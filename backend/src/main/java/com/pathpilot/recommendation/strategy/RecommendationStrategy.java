package com.pathpilot.recommendation.strategy;

import com.pathpilot.model.RecommendationResult;
import com.pathpilot.model.StudentProfile;

/**
 * Strategy interface defining the recommendation execution contract.
 */
public interface RecommendationStrategy {
    RecommendationResult recommend(StudentProfile student, int limit);
    String getStrategyName();
}
