package com.pathpilot.service;

import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;

import java.util.List;

public interface RecommendationService {
    RecommendationResult getRecommendationsForStudent(Long studentId, int limit);
    List<RecommendationItem> getLatestRecommendations(Long studentId);
}
