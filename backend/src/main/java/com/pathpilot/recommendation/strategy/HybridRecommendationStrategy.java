package com.pathpilot.recommendation.strategy;

import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.concurrency.RecommendationTaskExecutor;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.ExplainabilityService;
import com.pathpilot.recommendation.RecommendationContext;
import com.pathpilot.recommendation.rerank.RecommendationReranker;
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
 * scoring with concurrent execution, optional diversity reranking, and thread-safe telemetry.
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
    private final RecommendationReranker diversityReranker;

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
            RecommendationReranker diversityReranker,
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
        this.diversityReranker = diversityReranker;
        this.contentScorer = contentScorer;
        this.skillMatchScorer = skillMatchScorer;
        this.contextualScorer = contextualScorer;
        this.feedbackScorer = feedbackScorer;
    }

    @Override
    public RecommendationResult recommend(StudentProfile student, int limit) {
        return recommend(student, limit, true);
    }

    public RecommendationResult recommend(StudentProfile student, int limit, boolean applyDiversity) {
        log.info("Generating hybrid recommendations for student id={} (diverse={})", student.getId(), applyDiversity);

        List<UserFeedback> studentFeedbacks = student.getId() != null
                ? feedbackRepository.findByStudentId(student.getId())
                : List.of();

        RecommendationResult result = executeScoringPipeline(student, studentFeedbacks, limit, applyDiversity);

        if (student.getId() != null) {
            try {
                Long recId = recommendationRepository.saveRecommendation(student.getId(), getStrategyName(), result.getItems());
                result.setRecommendationId(recId);
            } catch (Exception e) {
                log.error("Failed to persist recommendation run for student id={}", student.getId(), e);
            }
        }
        return result;
    }

    /**
     * Evaluates a hypothetical profile in-memory without writing recommendations or profile changes to the database.
     */
    public RecommendationResult simulate(StudentProfile simulatedStudent, List<UserFeedback> existingFeedback,
                                         int limit, boolean applyDiversity) {
        return executeScoringPipeline(simulatedStudent, existingFeedback, limit, applyDiversity);
    }

    /**
     * Scores an explicit subset of opportunities for side-by-side comparison without persisting a recommendation batch.
     */
    public List<RecommendationItem> scoreSpecificOpportunities(StudentProfile student, List<Opportunity> opportunities) {
        if (opportunities == null || opportunities.isEmpty()) {
            return List.of();
        }
        List<UserFeedback> feedbacks = student.getId() != null
                ? feedbackRepository.findByStudentId(student.getId())
                : List.of();
        RecommendationContext context = buildContext(student, feedbacks, new HashSet<>());

        List<Supplier<RecommendationItem>> tasks = opportunities.stream()
                .map(opp -> (Supplier<RecommendationItem>) () -> scoreCandidate(student, opp, context))
                .toList();

        List<RecommendationItem> scored = new ArrayList<>(taskExecutor.executeParallelTasks(tasks));
        scored.sort(RecommendationItem.BY_RANKING_SCORE_DESC);
        for (int i = 0; i < scored.size(); i++) {
            scored.get(i).setRankOrder(i + 1);
        }
        return scored;
    }

    private RecommendationResult executeScoringPipeline(StudentProfile student, List<UserFeedback> studentFeedbacks,
                                                        int limit, boolean applyDiversity) {
        int clampedLimit = Math.max(1, Math.min(25, limit));

        Set<String> studentSkillNames = new HashSet<>(student.getSkillsMap().keySet());
        List<Opportunity> candidateOpportunities = opportunityRepository.findCandidatesByRoleAndInterests(
                student.getTargetRole(), studentSkillNames, Math.max(25, clampedLimit * 2)
        );

        Set<Long> dismissedOpportunityIds = new HashSet<>();
        RecommendationContext context = buildContext(student, studentFeedbacks, dismissedOpportunityIds);

        List<Opportunity> eligibleCandidates = candidateOpportunities.stream()
                .filter(opp -> !dismissedOpportunityIds.contains(opp.getId()))
                .filter(opp -> {
                    BigDecimal maxAllowed = student.getMaxBudget().multiply(BigDecimal.valueOf(1.5));
                    return opp.getCost().compareTo(maxAllowed) <= 0;
                })
                .toList();

        List<Supplier<RecommendationItem>> scoringTasks = eligibleCandidates.stream()
                .map(opp -> (Supplier<RecommendationItem>) () -> scoreCandidate(student, opp, context))
                .toList();

        List<RecommendationItem> scoredItems = new ArrayList<>(taskExecutor.executeParallelTasks(scoringTasks));
        scoredItems.sort(RecommendationItem.BY_RANKING_SCORE_DESC);

        List<RecommendationItem> finalItems;
        boolean rerankedApplied = false;
        if (applyDiversity && diversityReranker != null && scoredItems.size() > 1) {
            finalItems = diversityReranker.rerank(scoredItems, clampedLimit);
            rerankedApplied = true;
        } else {
            finalItems = scoredItems.stream().limit(clampedLimit).toList();
            for (int i = 0; i < finalItems.size(); i++) {
                finalItems.get(i).setRankOrder(i + 1);
            }
        }

        RecommendationResult result = new RecommendationResult(null, student.getId(), getStrategyName(), finalItems);
        boolean isColdStart = student.getSkillsMap().isEmpty();
        result.setColdStart(isColdStart);
        result.setDiversityReranked(rerankedApplied);
        if (isColdStart) {
            result.setRelevanceBasis(RelevanceBasis.LIMITED_DATA_COLD_START);
        } else if (!context.getStudentInteractedOpportunityIds().isEmpty()) {
            result.setRelevanceBasis(RelevanceBasis.FEEDBACK_SUPPORTED);
        } else {
            result.setRelevanceBasis(RelevanceBasis.PROFILE_BASED);
        }
        return result;
    }

    private RecommendationContext buildContext(StudentProfile student, List<UserFeedback> studentFeedbacks,
                                               Set<Long> dismissedOut) {
        RecommendationContext context = new RecommendationContext(student.getTargetRole());
        List<TargetRoleSkill> roleSkills = skillRepository.findRoleSkills(student.getTargetRole());
        context.setRoleSkills(roleSkills);
        context.setStudentSkills(student.getSkillsList());

        if (studentFeedbacks != null) {
            for (UserFeedback fb : studentFeedbacks) {
                if (fb.getActionType() == FeedbackAction.DISMISSED) {
                    dismissedOut.add(fb.getOpportunityId());
                } else if (fb.getActionType() == FeedbackAction.RATED && fb.getRatingValue() != null) {
                    context.setOpportunityFeedbackRating(fb.getOpportunityId(), fb.getRatingValue());
                }
                context.addInteractedOpportunity(fb.getOpportunityId());
            }
        }

        // Shift weight toward semantic content and context when no skills are recorded yet (weights sum to 1.00)
        if (student.getSkillsMap().isEmpty()) {
            context.setContentWeight(0.40);
            context.setSkillWeight(0.30);
            context.setContextualWeight(0.18);
            context.setFeedbackWeight(0.12);
        }
        return context;
    }

    private RecommendationItem scoreCandidate(StudentProfile student, Opportunity opp, RecommendationContext context) {
        double contentScore = contentScorer.score(student, opp, context);
        double skillScore = skillMatchScorer.score(student, opp, context);
        double contextualScore = contextualScorer.score(student, opp, context);
        double feedbackScore = feedbackScorer.score(student, opp, context);

        double rawRank = (context.getContentWeight() * contentScore)
                       + (context.getSkillWeight() * skillScore)
                       + (context.getContextualWeight() * contextualScore)
                       + (context.getFeedbackWeight() * feedbackScore);

        double telemetryMultiplier = telemetryAggregator.getFeedbackAdjustmentMultiplier(opp.getId());
        double finalScore = Math.max(0.0, Math.min(1.0, rawRank * telemetryMultiplier));

        ExplanationData explanation = explainabilityService.generateExplanation(student, opp, context);

        return new RecommendationItem(opp, finalScore, contentScore, skillScore, contextualScore, feedbackScore, explanation);
    }

    @Override
    public String getStrategyName() {
        return "HybridExplainableStrategy";
    }
}
