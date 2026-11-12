package com.pathpilot.service;

import com.pathpilot.dto.WhatIfSimulationRequestDto;
import com.pathpilot.dto.WhatIfSimulationResultDto;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;

import java.util.List;

public interface RecommendationService {
    RecommendationResult getRecommendationsForStudent(Long studentId, int limit);
    RecommendationResult getRecommendationsForStudent(Long studentId, int limit, boolean applyDiversity);
    List<RecommendationItem> getLatestRecommendations(Long studentId);
    List<RecommendationItem> compareOpportunities(Long studentId, List<Long> opportunityIds);
    WhatIfSimulationResultDto simulateWhatIf(Long studentId, WhatIfSimulationRequestDto request);
}
