package com.pathpilot.model;

import com.pathpilot.model.opportunity.Opportunity;

import java.util.Comparator;
import java.util.Objects;

/**
 * Encapsulates an individual ranked opportunity along with its normalized scoring components and explainability payload.
 */
public class RecommendationItem implements Comparable<RecommendationItem> {
    private Opportunity opportunity;
    private double rankingScore;
    private double contentScore;
    private double skillScore;
    private double contextualScore;
    private double feedbackScore;
    private int rankOrder;
    private ExplanationData explanation;

    public RecommendationItem() {}

    public RecommendationItem(Opportunity opportunity, double rankingScore, double contentScore,
                              double skillScore, double contextualScore, double feedbackScore,
                              ExplanationData explanation) {
        this.opportunity = Objects.requireNonNull(opportunity, "Opportunity cannot be null");
        this.rankingScore = rankingScore;
        this.contentScore = contentScore;
        this.skillScore = skillScore;
        this.contextualScore = contextualScore;
        this.feedbackScore = feedbackScore;
        this.explanation = explanation;
    }

    /**
     * Standard Comparator ordering recommendations by total ranking score descending,
     * breaking ties by user rating count and rating average.
     */
    public static final Comparator<RecommendationItem> BY_RANKING_SCORE_DESC = (a, b) -> {
        int cmp = Double.compare(b.getRankingScore(), a.getRankingScore());
        if (cmp != 0) return cmp;
        return Double.compare(b.getOpportunity().getRating(), a.getOpportunity().getRating());
    };

    @Override
    public int compareTo(RecommendationItem other) {
        return BY_RANKING_SCORE_DESC.compare(this, other);
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public double getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(double rankingScore) {
        this.rankingScore = rankingScore;
    }

    public double getContentScore() {
        return contentScore;
    }

    public void setContentScore(double contentScore) {
        this.contentScore = contentScore;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getContextualScore() {
        return contextualScore;
    }

    public void setContextualScore(double contextualScore) {
        this.contextualScore = contextualScore;
    }

    public double getFeedbackScore() {
        return feedbackScore;
    }

    public void setFeedbackScore(double feedbackScore) {
        this.feedbackScore = feedbackScore;
    }

    public int getRankOrder() {
        return rankOrder;
    }

    public void setRankOrder(int rankOrder) {
        this.rankOrder = rankOrder;
    }

    public ExplanationData getExplanation() {
        return explanation;
    }

    public void setExplanation(ExplanationData explanation) {
        this.explanation = explanation;
    }
}
