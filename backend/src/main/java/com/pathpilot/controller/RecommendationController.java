package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;
import com.pathpilot.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<RecommendationResult>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "8") int limit) {
        RecommendationResult result = recommendationService.getRecommendationsForStudent(studentId, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Generated personalized recommendations"));
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getLatestRecommendations(
            @PathVariable Long studentId) {
        List<RecommendationItem> items = recommendationService.getLatestRecommendations(studentId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
