package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.CompareRequestDto;
import com.pathpilot.dto.WhatIfSimulationRequestDto;
import com.pathpilot.dto.WhatIfSimulationResultDto;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;
import com.pathpilot.service.AuthSessionService;
import com.pathpilot.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AuthSessionService authSessionService;

    public RecommendationController(RecommendationService recommendationService,
                                    AuthSessionService authSessionService) {
        this.recommendationService = recommendationService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<RecommendationResult>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "true") boolean diverse,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        if (limit < 1 || limit > 25) {
            throw new InvalidRecommendationRequestException("Parameter 'limit' must be between 1 and 25.");
        }
        RecommendationResult result = recommendationService.getRecommendationsForStudent(studentId, limit, diverse);
        return ResponseEntity.ok(ApiResponse.success(result, "Generated personalized recommendations"));
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getLatestRecommendations(
            @PathVariable Long studentId,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        List<RecommendationItem> items = recommendationService.getLatestRecommendations(studentId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/student/{studentId}/compare")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> compareOpportunities(
            @PathVariable Long studentId,
            @Valid @RequestBody CompareRequestDto dto,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        List<RecommendationItem> compared = recommendationService.compareOpportunities(studentId, dto.getOpportunityIds());
        return ResponseEntity.ok(ApiResponse.success(compared, "Comparison breakdown prepared"));
    }

    @PostMapping("/student/{studentId}/simulate")
    public ResponseEntity<ApiResponse<WhatIfSimulationResultDto>> simulateCareerPath(
            @PathVariable Long studentId,
            @Valid @RequestBody WhatIfSimulationRequestDto dto,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        WhatIfSimulationResultDto simulated = recommendationService.simulateWhatIf(studentId, dto);
        return ResponseEntity.ok(ApiResponse.success(simulated, "What-if simulation computed without modifying profile"));
    }
}
