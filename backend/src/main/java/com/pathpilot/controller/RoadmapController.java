package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.LearningPlanUpdateDto;
import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<LearningPlanItem>>> getRoadmap(@PathVariable Long studentId) {
        List<LearningPlanItem> items = roadmapService.getRoadmap(studentId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/student/{studentId}/generate")
    public ResponseEntity<ApiResponse<List<LearningPlanItem>>> generateRoadmap(@PathVariable Long studentId) {
        List<LearningPlanItem> items = roadmapService.generateRoadmap(studentId);
        return ResponseEntity.ok(ApiResponse.success(items, "Roadmap regenerated successfully"));
    }

    @PatchMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<LearningPlanItem>> updateItem(
            @PathVariable Long itemId,
            @RequestBody LearningPlanUpdateDto dto) {
        LearningPlanItem updated = roadmapService.updateItemStatus(itemId, dto.getStatus(), dto.getTargetCompletionWeeks());
        return ResponseEntity.ok(ApiResponse.success(updated, "Roadmap progress updated"));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId) {
        roadmapService.deleteRoadmapItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from roadmap"));
    }
}
