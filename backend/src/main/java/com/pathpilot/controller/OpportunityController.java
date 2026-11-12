package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.OpportunityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;

    public OpportunityController(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Opportunity>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) BigDecimal maxCost,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        if (limit < 1 || limit > 100) {
            throw new InvalidRecommendationRequestException("Parameter 'limit' must be between 1 and 100.");
        }
        if (offset < 0) {
            throw new InvalidRecommendationRequestException("Parameter 'offset' cannot be negative.");
        }
        if (maxCost != null && maxCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRecommendationRequestException("Parameter 'maxCost' cannot be negative.");
        }
        List<Opportunity> results = opportunityRepository.search(query, type, difficulty, maxCost, limit, offset);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Opportunity>> getById(@PathVariable Long id) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));
        return ResponseEntity.ok(ApiResponse.success(opp));
    }
}
