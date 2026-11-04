package com.pathpilot.recommendation.scoring;

import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.RecommendationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Evaluates contextual and feasibility constraints:
 * - Budget suitability
 * - Time commitment vs. available weekly hours
 * - Polymorphic difficulty penalty dispatched to Opportunity subclasses
 */
@Component
public class ContextualScorer implements RecommendationScorer {

    @Override
    public double score(StudentProfile student, Opportunity opportunity, RecommendationContext context) {
        if (student == null || opportunity == null) return 0.0;

        // 1. Budget Suitability
        BigDecimal cost = opportunity.getCost();
        BigDecimal maxBudget = student.getMaxBudget();
        double budgetScore = 1.0;

        if (cost != null && maxBudget != null) {
            if (cost.compareTo(BigDecimal.ZERO) == 0) {
                budgetScore = 1.0; // Free opportunities get top financial score
            } else if (cost.compareTo(maxBudget) <= 0) {
                // Within budget, slight penalty for utilizing larger portion of budget
                double ratio = cost.doubleValue() / Math.max(1.0, maxBudget.doubleValue());
                budgetScore = 1.0 - (0.20 * ratio);
            } else {
                // Exceeds budget: steep linear decay
                double overage = cost.doubleValue() - maxBudget.doubleValue();
                budgetScore = Math.max(0.05, 0.60 - (overage / 100.0));
            }
        }

        // 2. Time Availability & Pace Fit
        double estimatedHours = opportunity.getEstimatedHours();
        int weeklyHours = student.getWeeklyHoursAvailable();
        double timeScore = 1.0;

        if (weeklyHours > 0) {
            // Assume completion window target of 4 to 12 weeks
            double hoursPerWeekNeeded = estimatedHours / 6.0;
            if (hoursPerWeekNeeded <= weeklyHours) {
                timeScore = 1.0;
            } else {
                // Requires more hours/week than student is willing to commit
                double deficitRatio = (hoursPerWeekNeeded - weeklyHours) / hoursPerWeekNeeded;
                timeScore = Math.max(0.20, 1.0 - deficitRatio);
            }
        }

        // 3. Dynamic Dispatch: Polymorphic Difficulty Penalty
        // Dispatches to CourseOpportunity, ProjectOpportunity, InternshipOpportunity, or CertificationOpportunity
        double difficultyPenalty = opportunity.calculateDifficultyPenalty(student);
        double difficultyScore = Math.max(0.10, 1.0 - difficultyPenalty);

        // Weighted contextual combination
        double total = (0.35 * budgetScore) + (0.35 * timeScore) + (0.30 * difficultyScore);
        return Math.max(0.0, Math.min(1.0, total));
    }

    @Override
    public String getScorerName() {
        return "ContextualScorer";
    }
}
