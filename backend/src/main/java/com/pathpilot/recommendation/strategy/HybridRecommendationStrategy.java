package com.pathpilot.recommendation.strategy;

import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.concurrency.RecommendationTaskExecutor;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.ExplainabilityService;
import com.pathpilot.recommendation.RecommendationContext;
import com.pathpilot.recommendation.scoring.ContentBasedScorer;
import com.pathpilot.recommendation.scoring.ContextualScorer;
import com.pathpilot.recommendation.scoring.FeedbackScorer;
import com.pathpilot.recommendation.scoring.SkillMatchScorer;
import com.pathpilot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

/**
 * High-performance Explainable Hybrid Recommendation Strategy.
 * Combines Content-based, Skill-gap match, Contextual constraints, and Bayesian feedback
 * scoring with concurrent execution and thread-safe real-time telemetry adjustment.
 */
@Component
@Primary
public class HybridRecommendationStrategy implements RecommendationStrategy {

    private static final Logger log = LoggerFactory.getLogger(HybridRecommendationStrategy.class);

    private final OpportunityRepository opportunityRepository;
    private final SkillRepository skillRepository;
    private final FeedbackRepository feedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationTaskExecutor taskExecutor;
    private final FeedbackTelemetryAggregator telemetryAggregator;
    private final ExplainabilityService explainabilityService;

    // Polymorphic scoring strategies
    private final ContentBasedScorer contentScorer;
    private final SkillMatchScorer skillMatchScorer;
    private final ContextualScorer contextualScorer;
    private final FeedbackScorer feedbackScorer;

    public HybridRecommendationStrategy(
            OpportunityRepository opportunityRepository,
            SkillRepository skillRepository,
            FeedbackRepository feedbackRepository,
            RecommendationRepository recommendationRepository,
            RecommendationTaskExecutor taskExecutor,
            FeedbackTelemetryAggregator telemetryAggregator,
            ExplainabilityService explainabilityService,
            ContentBasedScorer contentScorer,
            SkillMatchScorer skillMatchScorer,
            ContextualScorer contextualScorer,
            FeedbackScorer feedbackScorer) {
        this.opportunityRepository = opportunityRepository;
        this.skillRepository = skillRepository;
        this.feedbackRepository = feedbackRepository;
        this.recommendationRepository = recommendationRepository;
        this.taskExecutor = taskExecutor;
        this.telemetryAggregator = telemetryAggregator;
        this.explainabilityService = explainabilityService;
        this.contentScorer = contentScorer;
        this.skillMatchScorer = skillMatchScorer;
        this.contextualScorer = contextualScorer;
        this.feedbackScorer = feedbackScorer;
    }

    @Override
    public RecommendationResult recommend(StudentProfile student, int limit) {
        log.info("Generating hybrid recommendations for student: {} ({})", student.getFullName(), student.getEmail());

        // Step 1: Candidate Retrieval
        Set<String> studentSkillNames = new HashSet<>(student.getSkillsMap().keySet());
        List<Opportunity> candidateOpportunities = opportunityRepository.findCandidatesByRoleAndInterests(
                student.getTargetRole(), studentSkillNames, Math.max(25, limit * 2)
        );

        // Step 2: Context Preparation & Role Benchmark Loading
        RecommendationContext context = new RecommendationContext(student.getTargetRole());
        List<TargetRoleSkill> roleSkills = skillRepository.findRoleSkills(student.getTargetRole());
        context.setRoleSkills(roleSkills);
        context.setStudentSkills(student.getSkillsList());

        // Step 3: Feedback History Loading
        List<UserFeedback> studentFeedbacks = feedbackRepository.findByStudentId(student.getId());
        Set<Long> dismissedOpportunityIds = new HashSet<>();
        for (UserFeedback fb : studentFeedbacks) {
            if (fb.getActionType() == FeedbackAction.DISMISSED) {
                dismissedOpportunityIds.add(fb.getOpportunityId());
            } else if (fb.getActionType() == FeedbackAction.RATED && fb.getRatingValue() != null) {
                context.setOpportunityFeedbackRating(fb.getOpportunityId(), fb.getRatingValue());
            }
            context.addInteractedOpportunity(fb.getOpportunityId());
        }

        // Cold-Start Strategy: If the user has 0 skills recorded, adapt weights to balance exploration
        boolean isColdStart = student.getSkillsMap().isEmpty();
        if (isColdStart) {
            context.setContentWeight(0.35);
            context.setSkillWeight(0.20);
            context.setCareerWeight(0.25);
            context.setContextualWeight(0.10);
            context.setFeedbackWeight(0.10);
        }

        // Step 4: Eligibility Filtering (Filter dismissed and drastically over-budget items)
        List<Opportunity> eligibleCandidates = candidateOpportunities.stream()
                .filter(opp -> !dismissedOpportunityIds.contains(opp.getId()))
                .filter(opp -> {
                    // Strict budget filter: allow up to 1.5x user budget if score is high, otherwise discard
                    BigDecimal maxAllowed = student.getMaxBudget().multiply(BigDecimal.valueOf(1.5));
                    return opp.getCost().compareTo(maxAllowed) <= 0;
                })
                .toList();

        // Step 5: Concurrency - Parallel Scoring Tasks
        List<Supplier<RecommendationItem>> scoringTasks = eligibleCandidates.stream()
                .map(opp -> (Supplier<RecommendationItem>) () -> scoreCandidate(student, opp, context))
                .toList();

        List<RecommendationItem> scoredItems = new ArrayList<>(taskExecutor.executeParallelTasks(scoringTasks));

        // Step 6: Custom Sorting using Java Comparator
        scoredItems.sort(RecommendationItem.BY_RANKING_SCORE_DESC);

        // Take top-K results
        List<RecommendationItem> topResults = scoredItems.stream()
                .limit(Math.max(1, limit))
                .toList();

        // Assign ordinal rank orders
        for (int i = 0; i < topResults.size(); i++) {
            topResults.get(i).setRankOrder(i + 1);
        }

        // Step 7: Persistence
        Long recId = null;
        try {
            recId = recommendationRepository.saveRecommendation(student.getId(), getStrategyName(), topResults);
        } catch (Exception e) {
            log.error("Failed to persist recommendation run to database", e);
        }

        return new RecommendationResult(recId, student.getId(), getStrategyName(), topResults);
    }

    private RecommendationItem scoreCandidate(StudentProfile student, Opportunity opp, RecommendationContext context) {
        // Compute individual polymorphic scores
        double contentScore = contentScorer.score(student, opp, context);
        double skillScore = skillMatchScorer.score(student, opp, context);
        double contextualScore = contextualScorer.score(student, opp, context);
        double feedbackScore = feedbackScorer.score(student, opp, context);

        // Base weighted hybrid ranking formula
        double rawRank = (context.getContentWeight() * contentScore)
                       + (context.getSkillWeight() * skillScore)
                       + (context.getContextualWeight() * contextualScore)
                       + (context.getFeedbackWeight() * feedbackScore);

        // Apply thread-safe real-time telemetry multiplier
        double telemetryMultiplier = telemetryAggregator.getFeedbackAdjustmentMultiplier(opp.getId());
        double finalScore = Math.max(0.0, Math.min(1.0, rawRank * telemetryMultiplier));

        // Generate explainability breakdown
        ExplanationData explanation = explainabilityService.generateExplanation(student, opp, context);

        return new RecommendationItem(opp, finalScore, contentScore, skillScore, contextualScore, feedbackScore, explanation);
    }

    @Override
    public String getStrategyName() {
        return "HybridExplainableStrategy";
    }
}
