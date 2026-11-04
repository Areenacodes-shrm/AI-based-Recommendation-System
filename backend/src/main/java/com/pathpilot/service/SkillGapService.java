package com.pathpilot.service;

import com.pathpilot.dto.SkillGapAnalysisDto;

public interface SkillGapService {
    SkillGapAnalysisDto analyzeSkillGap(Long studentId, String targetRole);
}
