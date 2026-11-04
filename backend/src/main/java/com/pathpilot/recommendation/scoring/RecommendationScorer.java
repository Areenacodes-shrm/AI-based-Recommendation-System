package com.pathpilot.recommendation.scoring;

import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.RecommendationContext;

/**
 * Polymorphic strategy contract for computing specialized recommendation scores.
 * Every scorer returns a normalized value in the range [0.0, 1.0].
 */
public interface RecommendationScorer {
    /**
     * Compute a normalized score [0.0, 1.0] for the given student and opportunity.
     */
    double score(StudentProfile student, Opportunity opportunity, RecommendationContext context);

    /**
     * Human-readable identifier for this scoring strategy.
     */
    String getScorerName();
}
