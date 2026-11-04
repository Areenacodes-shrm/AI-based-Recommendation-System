package com.pathpilot.recommendation.scoring;

import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.RecommendationContext;
import org.springframework.stereotype.Component;

/**
 * Evaluates crowd ratings using Bayesian smoothing and historical student interactions.
 */
@Component
public class FeedbackScorer implements RecommendationScorer {

    // Bayesian prior parameters: m = confidence threshold count, C = global mean rating prior
    private static final double CONFIDENCE_THRESHOLD_M = 10.0;
    private static final double GLOBAL_PRIOR_C = 4.0;

    @Override
    public double score(StudentProfile student, Opportunity opportunity, RecommendationContext context) {
        if (opportunity == null) return 0.5;

        // 1. Bayesian crowd rating smoothing
        double rawRating = opportunity.getRating();
        int count = opportunity.getRatingCount();

        // Formula: (v * R + m * C) / (v + m)
        double bayesianRating = ((count * rawRating) + (CONFIDENCE_THRESHOLD_M * GLOBAL_PRIOR_C))
                / (count + CONFIDENCE_THRESHOLD_M);

        // Normalize 5-star scale to [0.0, 1.0]
        double normalizedRating = bayesianRating / 5.0;

        // 2. Telemetry and explicit interaction adjustment
        if (context != null) {
            // Check if student has explicit interactions
            if (context.getOpportunityFeedbackRatings().containsKey(opportunity.getId())) {
                double userRating = context.getOpportunityFeedbackRatings().get(opportunity.getId());
                // Blend user rating heavily
                normalizedRating = (0.4 * normalizedRating) + (0.6 * (userRating / 5.0));
            }
        }

        return Math.max(0.0, Math.min(1.0, normalizedRating));
    }

    @Override
    public String getScorerName() {
        return "FeedbackScorer";
    }
}
