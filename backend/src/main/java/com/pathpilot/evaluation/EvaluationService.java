package com.pathpilot.evaluation;

import com.pathpilot.dto.EvaluationResultDto;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;
import com.pathpilot.model.Skill;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.TargetRoleSkill;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.strategy.HybridRecommendationStrategy;
import com.pathpilot.recommendation.strategy.PopularityBaselineStrategy;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.SkillRepository;
import com.pathpilot.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Offline Recommendation Evaluation Framework.
 * Computes standard information retrieval metrics:
 * - Precision@K
 * - Recall@K
 * - NDCG@K (Normalized Discounted Cumulative Gain)
 * - Catalog Coverage
 * Compares PathPilot Hybrid AI against a Popularity Baseline.
 */
@Service
public class EvaluationService {

    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final SkillRepository skillRepository;
    private final HybridRecommendationStrategy hybridStrategy;
    private final PopularityBaselineStrategy baselineStrategy;

    public EvaluationService(StudentRepository studentRepository,
                             OpportunityRepository opportunityRepository,
                             SkillRepository skillRepository,
                             HybridRecommendationStrategy hybridStrategy,
                             PopularityBaselineStrategy baselineStrategy) {
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.skillRepository = skillRepository;
        this.hybridStrategy = hybridStrategy;
        this.baselineStrategy = baselineStrategy;
    }

    public EvaluationResultDto evaluate(int topK) {
        int k = Math.max(1, topK);
        List<StudentProfile> students = studentRepository.findAll();
        List<Opportunity> allCatalog = opportunityRepository.findAll();
        int totalCatalogCount = allCatalog.size();

        if (students.isEmpty() || totalCatalogCount == 0) {
            return new EvaluationResultDto();
        }

        double totalHybridPrec = 0.0;
        double totalHybridRecall = 0.0;
        double totalHybridNdcg = 0.0;
        Set<Long> hybridRecommendedOppIds = new HashSet<>();

        double totalBasePrec = 0.0;
        double totalBaseRecall = 0.0;
        double totalBaseNdcg = 0.0;
        Set<Long> baseRecommendedOppIds = new HashSet<>();

        for (StudentProfile student : students) {
            // Ground truth definition: Opportunities teaching core skills for the student's target role
            List<TargetRoleSkill> roleSkills = skillRepository.findRoleSkills(student.getTargetRole());
            Set<String> coreSkillNames = new HashSet<>();
            for (TargetRoleSkill trs : roleSkills) {
                if (trs.isCore()) {
                    coreSkillNames.add(trs.getSkill().getName().toLowerCase());
                }
            }

            Set<Long> relevantOppIds = new HashSet<>();
            for (Opportunity opp : allCatalog) {
                boolean teachesCore = opp.getSkillsGained().stream()
                        .anyMatch(s -> coreSkillNames.contains(s.getName().toLowerCase()));
                if (teachesCore) {
                    relevantOppIds.add(opp.getId());
                }
            }

            // Hybrid recommendations
            RecommendationResult hybridResult = hybridStrategy.recommend(student, k);
            List<RecommendationItem> hybridItems = hybridResult.getItems();
            totalHybridPrec += computePrecisionAtK(hybridItems, relevantOppIds, k);
            totalHybridRecall += computeRecallAtK(hybridItems, relevantOppIds);
            totalHybridNdcg += computeNdcgAtK(hybridItems, relevantOppIds, k);
            for (RecommendationItem it : hybridItems) {
                hybridRecommendedOppIds.add(it.getOpportunity().getId());
            }

            // Baseline recommendations
            RecommendationResult baselineResult = baselineStrategy.recommend(student, k);
            List<RecommendationItem> baseItems = baselineResult.getItems();
            totalBasePrec += computePrecisionAtK(baseItems, relevantOppIds, k);
            totalBaseRecall += computeRecallAtK(baseItems, relevantOppIds);
            totalBaseNdcg += computeNdcgAtK(baseItems, relevantOppIds, k);
            for (RecommendationItem it : baseItems) {
                baseRecommendedOppIds.add(it.getOpportunity().getId());
            }
        }

        int n = students.size();
        EvaluationResultDto dto = new EvaluationResultDto();
        dto.setTopK(k);
        dto.setTotalTestUsers(n);

        dto.setHybridPrecisionAtK(round(totalHybridPrec / n));
        dto.setHybridRecallAtK(round(totalHybridRecall / n));
        dto.setHybridNdcgAtK(round(totalHybridNdcg / n));
        dto.setHybridCatalogCoverage(round((double) hybridRecommendedOppIds.size() / totalCatalogCount * 100.0));

        dto.setBaselinePrecisionAtK(round(totalBasePrec / n));
        dto.setBaselineRecallAtK(round(totalBaseRecall / n));
        dto.setBaselineNdcgAtK(round(totalBaseNdcg / n));
        dto.setBaselineCatalogCoverage(round((double) baseRecommendedOppIds.size() / totalCatalogCount * 100.0));

        return dto;
    }

    private double computePrecisionAtK(List<RecommendationItem> items, Set<Long> relevantIds, int k) {
        if (items.isEmpty() || k <= 0) return 0.0;
        int hitCount = 0;
        for (int i = 0; i < Math.min(items.size(), k); i++) {
            if (relevantIds.contains(items.get(i).getOpportunity().getId())) {
                hitCount++;
            }
        }
        return (double) hitCount / k;
    }

    private double computeRecallAtK(List<RecommendationItem> items, Set<Long> relevantIds) {
        if (relevantIds.isEmpty()) return 1.0;
        int hitCount = 0;
        for (RecommendationItem item : items) {
            if (relevantIds.contains(item.getOpportunity().getId())) {
                hitCount++;
            }
        }
        return (double) hitCount / relevantIds.size();
    }

    private double computeNdcgAtK(List<RecommendationItem> items, Set<Long> relevantIds, int k) {
        if (relevantIds.isEmpty() || items.isEmpty()) return 0.0;

        double dcg = 0.0;
        for (int i = 0; i < Math.min(items.size(), k); i++) {
            boolean isRel = relevantIds.contains(items.get(i).getOpportunity().getId());
            if (isRel) {
                // DCG formula: rel_i / log2(i + 2)
                dcg += 1.0 / (Math.log(i + 2) / Math.log(2.0));
            }
        }

        // Ideal DCG (all relevant items at top positions)
        double idcg = 0.0;
        int maxPossible = Math.min(relevantIds.size(), k);
        for (int i = 0; i < maxPossible; i++) {
            idcg += 1.0 / (Math.log(i + 2) / Math.log(2.0));
        }

        return idcg > 0 ? (dcg / idcg) : 0.0;
    }

    private double round(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }
}
