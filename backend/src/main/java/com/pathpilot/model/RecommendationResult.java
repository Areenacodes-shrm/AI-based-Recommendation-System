package com.pathpilot.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Complete recommendation response bundle including batch metadata and ranked items.
 */
public class RecommendationResult {
    private Long recommendationId;
    private Long studentId;
    private String strategyUsed;
    private LocalDateTime generatedAt;
    private List<RecommendationItem> items = new ArrayList<>();

    public RecommendationResult() {
        this.generatedAt = LocalDateTime.now();
    }

    public RecommendationResult(Long recommendationId, Long studentId, String strategyUsed, List<RecommendationItem> items) {
        this.recommendationId = recommendationId;
        this.studentId = studentId;
        this.strategyUsed = strategyUsed;
        this.items = items != null ? items : new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public Long getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Long recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<RecommendationItem> getItems() {
        return items;
    }

    public void setItems(List<RecommendationItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
