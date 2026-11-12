package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.EvaluationResultDto;
import com.pathpilot.evaluation.EvaluationService;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/run")
    public ResponseEntity<ApiResponse<EvaluationResultDto>> runEvaluation(
            @RequestParam(defaultValue = "5") int topK) {
        if (topK < 1 || topK > 25) {
            throw new InvalidRecommendationRequestException("Parameter 'topK' must be between 1 and 25.");
        }
        EvaluationResultDto result = evaluationService.evaluate(topK);
        return ResponseEntity.ok(ApiResponse.success(result, "Offline evaluation benchmark executed successfully"));
    }
}
