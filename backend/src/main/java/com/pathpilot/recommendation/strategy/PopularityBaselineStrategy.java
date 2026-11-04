package com.pathpilot.recommendation.strategy;

import com.pathpilot.model.ExplanationData;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.OpportunityRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Baseline Popularity Recommender for comparative evaluation benchmarks.
 * Evaluates opportunities purely by aggregate platform rating and rating count.
 */
@Component
public class PopularityBaselineStrategy implements RecommendationStrategy {

    private final OpportunityRepository opportunityRepository;

    public PopularityBaselineStrategy(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    public RecommendationResult recommend(StudentProfile student, int limit) {
        List<Opportunity> all = opportunityRepository.findAll();

        List<RecommendationItem> items = new ArrayList<>();
        for (Opportunity opp : all) {
            // Popularity formula: rating * log(ratingCount + 1) normalized
            double rawScore = opp.getRating() * Math.log10(opp.getRatingCount() + 1);
            double normalized = Math.min(1.0, rawScore / (5.0 * Math.log10(1500)));

            ExplanationData exp = new ExplanationData();
            exp.setCareerRelevanceReason("Recommended by baseline system based on overall platform popularity.");
            exp.setTimeAndBudgetFitReason("Popularity rank independent of individual constraints.");

            RecommendationItem item = new RecommendationItem(opp, normalized, 0.5, 0.5, 0.5, normalized, exp);
            items.add(item);
        }

        // Sort descending
        items.sort(RecommendationItem.BY_RANKING_SCORE_DESC);

        List<RecommendationItem> topItems = items.stream().limit(limit).toList();
        for (int i = 0; i < topItems.size(); i++) {
            topItems.get(i).setRankOrder(i + 1);
        }

        return new RecommendationResult(null, student != null ? student.getId() : null, getStrategyName(), topItems);
    }

    @Override
    public String getStrategyName() {
        return "PopularityBaselineStrategy";
    }
}
