package com.pathpilot.service.impl;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.dto.StudentSkillInputDto;
import com.pathpilot.dto.WhatIfSimulationRequestDto;
import com.pathpilot.dto.WhatIfSimulationResultDto;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.strategy.HybridRecommendationStrategy;
import com.pathpilot.repository.FeedbackRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.RecommendationRepository;
import com.pathpilot.repository.SkillRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.RecommendationService;
import com.pathpilot.service.SkillGapService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentRepository studentRepository;
    private final HybridRecommendationStrategy hybridStrategy;
    private final RecommendationRepository recommendationRepository;
    private final OpportunityRepository opportunityRepository;
    private final SkillRepository skillRepository;
    private final FeedbackRepository feedbackRepository;
    private final SkillGapService skillGapService;

    public RecommendationServiceImpl(StudentRepository studentRepository,
                                     HybridRecommendationStrategy hybridStrategy,
                                     RecommendationRepository recommendationRepository,
                                     OpportunityRepository opportunityRepository,
                                     SkillRepository skillRepository,
                                     FeedbackRepository feedbackRepository,
                                     SkillGapService skillGapService) {
        this.studentRepository = studentRepository;
        this.hybridStrategy = hybridStrategy;
        this.recommendationRepository = recommendationRepository;
        this.opportunityRepository = opportunityRepository;
        this.skillRepository = skillRepository;
        this.feedbackRepository = feedbackRepository;
        this.skillGapService = skillGapService;
    }

    @Override
    public RecommendationResult getRecommendationsForStudent(Long studentId, int limit) {
        return getRecommendationsForStudent(studentId, limit, true);
    }

    @Override
    public RecommendationResult getRecommendationsForStudent(Long studentId, int limit, boolean applyDiversity) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return hybridStrategy.recommend(student, limit, applyDiversity);
    }

    @Override
    public List<RecommendationItem> getLatestRecommendations(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return recommendationRepository.findLatestByStudentId(studentId);
    }

    @Override
    public List<RecommendationItem> compareOpportunities(Long studentId, List<Long> opportunityIds) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        if (opportunityIds == null || opportunityIds.isEmpty()) {
            throw new InvalidRecommendationRequestException("Please select between 2 and 3 opportunities to compare.");
        }

        List<Long> uniqueIds = opportunityIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uniqueIds.size() < 2 || uniqueIds.size() > 3) {
            throw new InvalidRecommendationRequestException("Comparison requires 2 or 3 distinct opportunities.");
        }

        List<Opportunity> opportunities = opportunityRepository.findByIds(uniqueIds);
        if (opportunities.size() != uniqueIds.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Opportunity o : opportunities) {
                foundIds.add(o.getId());
            }
            for (Long reqId : uniqueIds) {
                if (!foundIds.contains(reqId)) {
                    throw new ResourceNotFoundException("Opportunity", reqId);
                }
            }
        }

        return hybridStrategy.scoreSpecificOpportunities(student, opportunities);
    }

    @Override
    public WhatIfSimulationResultDto simulateWhatIf(Long studentId, WhatIfSimulationRequestDto request) {
        StudentProfile baseline = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        List<UserFeedback> existingFeedback = feedbackRepository.findByStudentId(studentId);

        int limit = Math.max(1, Math.min(25, request.getLimit()));
        boolean applyDiversity = request.isApplyDiversity();

        // Compute non-persisted baseline slate so delta metrics compare equivalent runs cleanly
        RecommendationResult baselineResult = hybridStrategy.simulate(baseline, existingFeedback, Math.max(20, limit), applyDiversity);
        SkillGapAnalysisDto baselineGap = skillGapService.analyzeSkillGapForProfile(baseline, baseline.getTargetRole());

        // Construct isolated in-memory profile copy; database rows remain untouched
        StudentProfile simulated = baseline.copyForSimulation();
        if (request.getTargetRole() != null && !request.getTargetRole().isBlank()) {
            simulated.setTargetRole(request.getTargetRole().trim());
        }
        if (request.getWeeklyHoursAvailable() != null) {
            simulated.setWeeklyHoursAvailable(request.getWeeklyHoursAvailable());
        }
        if (request.getMaxBudget() != null) {
            simulated.setMaxBudget(request.getMaxBudget());
        }
        if (request.getPreferredDifficulty() != null) {
            simulated.setPreferredDifficulty(request.getPreferredDifficulty());
        }
        if (request.getBio() != null) {
            simulated.setBio(request.getBio());
        }
        if (request.getSimulatedSkills() != null) {
            List<StudentSkill> simSkills = new ArrayList<>();
            for (StudentSkillInputDto sInput : request.getSimulatedSkills()) {
                Optional<Skill> skillOpt;
                if (sInput.getSkillId() != null) {
                    skillOpt = skillRepository.findById(sInput.getSkillId());
                    if (skillOpt.isEmpty()) {
                        throw new InvalidRecommendationRequestException("Unknown skill ID in simulation: " + sInput.getSkillId());
                    }
                } else if (sInput.getSkillName() != null && !sInput.getSkillName().isBlank()) {
                    skillOpt = skillRepository.findByName(sInput.getSkillName());
                    if (skillOpt.isEmpty()) {
                        throw new InvalidRecommendationRequestException("Unknown skill name in simulation: " + sInput.getSkillName());
                    }
                } else {
                    continue;
                }
                simSkills.add(new StudentSkill(
                        simulated.getId(), skillOpt.get(), sInput.getProficiencyLevel(), sInput.getYearsExperience(), true
                ));
            }
            simulated.setSkills(simSkills);
        }

        RecommendationResult simulatedResult = hybridStrategy.simulate(simulated, existingFeedback, limit, applyDiversity);
        SkillGapAnalysisDto simulatedGap = skillGapService.analyzeSkillGapForProfile(simulated, simulated.getTargetRole());

        Map<Long, RecommendationItem> baselineByOppId = new HashMap<>();
        for (RecommendationItem item : baselineResult.getItems()) {
            baselineByOppId.put(item.getOpportunity().getId(), item);
        }

        WhatIfSimulationResultDto resultDto = new WhatIfSimulationResultDto();
        resultDto.setStudentId(studentId);
        resultDto.setBaselineTargetRole(baseline.getTargetRole());
        resultDto.setSimulatedTargetRole(simulated.getTargetRole());
        resultDto.setBaselineWeeklyHours(baseline.getWeeklyHoursAvailable());
        resultDto.setSimulatedWeeklyHours(simulated.getWeeklyHoursAvailable());
        resultDto.setBaselineMaxBudget(baseline.getMaxBudget());
        resultDto.setSimulatedMaxBudget(simulated.getMaxBudget());
        resultDto.setSimulatedDifficulty(simulated.getPreferredDifficulty());
        resultDto.setBaselineRoleMatchPercentage(baselineGap.getMatchPercentage());
        resultDto.setSimulatedRoleMatchPercentage(simulatedGap.getMatchPercentage());
        resultDto.setRoleMatchDelta(Math.round((simulatedGap.getMatchPercentage() - baselineGap.getMatchPercentage()) * 10.0) / 10.0);
        resultDto.setRelevanceBasis(simulatedResult.getRelevanceBasis());
        resultDto.setPersisted(false);

        List<WhatIfSimulationResultDto.SimulationItemDeltaDto> deltaItems = new ArrayList<>();
        for (RecommendationItem simItem : simulatedResult.getItems()) {
            RecommendationItem baseItem = baselineByOppId.get(simItem.getOpportunity().getId());
            Integer baseRank = baseItem != null ? baseItem.getRankOrder() : null;
            double baseScore = baseItem != null ? baseItem.getRankingScore() : 0.0;
            double scoreDelta = simItem.getRankingScore() - baseScore;

            String changeStatus;
            if (baseRank == null) {
                changeStatus = "NEW_ENTRY";
            } else if (baseRank > simItem.getRankOrder()) {
                changeStatus = "MOVED_UP";
            } else if (baseRank < simItem.getRankOrder()) {
                changeStatus = "MOVED_DOWN";
            } else {
                changeStatus = "UNCHANGED";
            }

            deltaItems.add(new WhatIfSimulationResultDto.SimulationItemDeltaDto(
                    simItem, baseRank, simItem.getRankOrder(), baseScore, scoreDelta, changeStatus
            ));
        }

        resultDto.setItems(deltaItems);
        return resultDto;
    }
}
