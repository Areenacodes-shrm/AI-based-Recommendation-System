package com.pathpilot.recommendation.rerank;

import com.pathpilot.model.RecommendationItem;

import java.util.List;

/**
 * Contract for post-scoring re-ranking passes (such as MMR diversity optimization).
 */
public interface RecommendationReranker {
    List<RecommendationItem> rerank(List<RecommendationItem> scoredCandidates, int limit);
}
