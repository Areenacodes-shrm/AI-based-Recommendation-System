package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.service.AuthSessionService;
import com.pathpilot.service.SkillGapService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill-gap")
public class SkillGapController {

    private final SkillGapService skillGapService;
    private final AuthSessionService authSessionService;

    public SkillGapController(SkillGapService skillGapService, AuthSessionService authSessionService) {
        this.skillGapService = skillGapService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<SkillGapAnalysisDto>> getSkillGap(
            @PathVariable Long studentId,
            @RequestParam(required = false) String targetRole,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        SkillGapAnalysisDto analysis = skillGapService.analyzeSkillGap(studentId, targetRole);
        return ResponseEntity.ok(ApiResponse.success(analysis, "Skill gap analysis completed"));
    }
}
