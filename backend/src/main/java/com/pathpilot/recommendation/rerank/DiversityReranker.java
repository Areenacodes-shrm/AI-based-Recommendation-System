package com.pathpilot.recommendation.rerank;

import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.Skill;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maximal Marginal Relevance (MMR) inspired diversity reranker.
 * Keeps heuristic relevance as the primary signal while preventing top-K
 * slates from collapsing into near-duplicate courses teaching the same skills.
 */
@Component
public class DiversityReranker implements RecommendationReranker {

    private static final double RELEVANCE_LAMBDA = 0.72;
    private static final double DIVERSITY_PENALTY_WEIGHT = 0.28;

    @Override
    public List<RecommendationItem> rerank(List<RecommendationItem> scoredCandidates, int limit) {
        if (scoredCandidates == null || scoredCandidates.isEmpty()) {
            return List.of();
        }

        int targetSize = Math.max(1, Math.min(limit, scoredCandidates.size()));
        List<RecommendationItem> remaining = new ArrayList<>(scoredCandidates);
        remaining.sort(RecommendationItem.BY_RANKING_SCORE_DESC);

        // Precompute normalized skill-name sets once so greedy selection stays fast
        Map<RecommendationItem, Set<String>> skillSets = new IdentityHashMap<>();
        for (RecommendationItem item : remaining) {
            Set<String> names = item.getOpportunity().getSkillsGained().stream()
                    .map(Skill::getName)
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .collect(Collectors.toSet());
            skillSets.put(item, names);
        }

        List<RecommendationItem> selected = new ArrayList<>(targetSize);
        Map<OpportunityType, Integer> selectedTypeCounts = new EnumMap<>(OpportunityType.class);

        // Top rank always honors the highest relevance score directly
        RecommendationItem first = remaining.remove(0);
        selected.add(first);
        selectedTypeCounts.merge(first.getOpportunity().getOpportunityType(), 1, Integer::sum);

        while (selected.size() < targetSize && !remaining.isEmpty()) {
            RecommendationItem bestCandidate = null;
            double bestUtility = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;

            for (int i = 0; i < remaining.size(); i++) {
                RecommendationItem candidate = remaining.get(i);
                double redundancy = computeRedundancy(candidate, selected, selectedTypeCounts, skillSets);
                double utility = (RELEVANCE_LAMBDA * candidate.getRankingScore())
                               - (DIVERSITY_PENALTY_WEIGHT * redundancy);

                if (bestCandidate == null
                        || utility > bestUtility + 1e-9
                        || (Math.abs(utility - bestUtility) <= 1e-9
                            && candidate.getRankingScore() > bestCandidate.getRankingScore())) {
                    bestCandidate = candidate;
                    bestUtility = utility;
                    bestIndex = i;
                }
            }

            if (bestIndex < 0) break;
            RecommendationItem picked = remaining.remove(bestIndex);
            selected.add(picked);
            selectedTypeCounts.merge(picked.getOpportunity().getOpportunityType(), 1, Integer::sum);
        }

        for (int i = 0; i < selected.size(); i++) {
            selected.get(i).setRankOrder(i + 1);
        }
        return selected;
    }

    private double computeRedundancy(RecommendationItem candidate,
                                     List<RecommendationItem> selected,
                                     Map<OpportunityType, Integer> selectedTypeCounts,
                                     Map<RecommendationItem, Set<String>> skillSets) {
        OpportunityType type = candidate.getOpportunity().getOpportunityType();
        int sameTypeCount = selectedTypeCounts.getOrDefault(type, 0);
        double typeSaturation = Math.min(1.0, sameTypeCount * 0.35);

        Set<String> candidateSkills = skillSets.getOrDefault(candidate, Set.of());
        double maxSkillJaccard = 0.0;
        if (!candidateSkills.isEmpty()) {
            for (RecommendationItem prev : selected) {
                Set<String> prevSkills = skillSets.getOrDefault(prev, Set.of());
                if (prevSkills.isEmpty()) continue;

                int intersection = 0;
                for (String s : candidateSkills) {
                    if (prevSkills.contains(s)) intersection++;
                }
                int union = candidateSkills.size() + prevSkills.size() - intersection;
                if (union > 0) {
                    maxSkillJaccard = Math.max(maxSkillJaccard, (double) intersection / union);
                }
            }
        }

        return (0.55 * typeSaturation) + (0.45 * maxSkillJaccard);
    }
}
