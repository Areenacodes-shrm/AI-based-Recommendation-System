package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.LearningPlanUpdateDto;
import com.pathpilot.dto.RoadmapAddRequestDto;
import com.pathpilot.dto.WeeklyProgressSummaryDto;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.service.AuthSessionService;
import com.pathpilot.service.RoadmapService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final AuthSessionService authSessionService;

    public RoadmapController(RoadmapService roadmapService, AuthSessionService authSessionService) {
        this.roadmapService = roadmapService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<LearningPlanItem>>> getRoadmap(
            @PathVariable Long studentId,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        List<LearningPlanItem> items = roadmapService.getRoadmap(studentId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/student/{studentId}/generate")
    public ResponseEntity<ApiResponse<List<LearningPlanItem>>> generateRoadmap(
            @PathVariable Long studentId,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        List<LearningPlanItem> items = roadmapService.generateRoadmap(studentId);
        return ResponseEntity.ok(ApiResponse.success(items, "Roadmap regenerated successfully"));
    }

    @PostMapping("/student/{studentId}/add")
    public ResponseEntity<ApiResponse<LearningPlanItem>> addOpportunityToRoadmap(
            @PathVariable Long studentId,
            @Valid @RequestBody RoadmapAddRequestDto dto,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        LearningPlanItem added = roadmapService.addOpportunityToRoadmap(studentId, dto.getOpportunityId(), dto.getNote());
        return ResponseEntity.ok(ApiResponse.success(added, "Opportunity added to your learning roadmap"));
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<ApiResponse<WeeklyProgressSummaryDto>> getWeeklyProgressSummary(
            @PathVariable Long studentId,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        WeeklyProgressSummaryDto summary = roadmapService.getWeeklyProgressSummary(studentId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/student/{studentId}/export")
    public ResponseEntity<byte[]> exportRoadmap(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        if (!"csv".equalsIgnoreCase(format.trim())) {
            throw new InvalidRecommendationRequestException("Unsupported export format '" + format + "'. Supported format: csv.");
        }
        String csv = roadmapService.exportRoadmapCsv(studentId);
        byte[] content = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pathpilot-roadmap-student-" + studentId + ".csv\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(content.length)
                .body(content);
    }

    @PatchMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<LearningPlanItem>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody LearningPlanUpdateDto dto,
            HttpServletRequest request) {
        LearningPlanItem existing = roadmapService.getItemById(itemId);
        authSessionService.requireAuthorizedStudent(request, existing.getStudentId());
        LearningPlanItem updated = roadmapService.updateItemStatus(itemId, dto.getStatus(), dto.getTargetCompletionWeeks());
        return ResponseEntity.ok(ApiResponse.success(updated, "Roadmap progress updated"));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long itemId,
            HttpServletRequest request) {
        LearningPlanItem existing = roadmapService.getItemById(itemId);
        authSessionService.requireAuthorizedStudent(request, existing.getStudentId());
        roadmapService.deleteRoadmapItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from roadmap"));
    }
}
