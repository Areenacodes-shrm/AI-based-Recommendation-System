package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.EvaluationResultDto;
import com.pathpilot.evaluation.EvaluationService;
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
        EvaluationResultDto result = evaluationService.evaluate(topK);
        return ResponseEntity.ok(ApiResponse.success(result, "Offline evaluation benchmark executed successfully"));
    }
}
