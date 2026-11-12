package com.pathpilot.service;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.model.StudentProfile;

public interface SkillGapService {
    SkillGapAnalysisDto analyzeSkillGap(Long studentId, String targetRole);
    SkillGapAnalysisDto analyzeSkillGapForProfile(StudentProfile student, String targetRole);
}
