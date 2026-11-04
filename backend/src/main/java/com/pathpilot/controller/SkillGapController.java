package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill-gap")
public class SkillGapController {

    private final SkillGapService skillGapService;

    public SkillGapController(SkillGapService skillGapService) {
        this.skillGapService = skillGapService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<SkillGapAnalysisDto>> getSkillGap(
            @PathVariable Long studentId,
            @RequestParam(required = false) String targetRole) {
        SkillGapAnalysisDto analysis = skillGapService.analyzeSkillGap(studentId, targetRole);
        return ResponseEntity.ok(ApiResponse.success(analysis, "Skill gap analysis completed"));
    }
}
