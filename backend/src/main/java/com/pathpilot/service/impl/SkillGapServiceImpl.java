package com.pathpilot.service.impl;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.ProficiencyLevel;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.StudentSkill;
import com.pathpilot.model.TargetRoleSkill;
import com.pathpilot.repository.SkillRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.SkillGapService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SkillGapServiceImpl implements SkillGapService {

    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;

    public SkillGapServiceImpl(StudentRepository studentRepository, SkillRepository skillRepository) {
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillGapAnalysisDto analyzeSkillGap(Long studentId, String targetRole) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : student.getTargetRole();
        List<TargetRoleSkill> requiredRoleSkills = skillRepository.findRoleSkills(role);

        SkillGapAnalysisDto analysis = new SkillGapAnalysisDto();
        analysis.setTargetRole(role);
        analysis.setTotalRequiredSkills(requiredRoleSkills.size());

        Map<String, StudentSkill> studentSkills = student.getSkillsMap();

        List<SkillGapAnalysisDto.SkillItemDto> matched = new ArrayList<>();
        List<SkillGapAnalysisDto.SkillItemDto> weak = new ArrayList<>();
        List<SkillGapAnalysisDto.SkillItemDto> missing = new ArrayList<>();

        double totalWeight = 0.0;
        double accumulatedProficiency = 0.0;

        for (TargetRoleSkill trs : requiredRoleSkills) {
            String skillName = trs.getSkill().getName();
            String skillKey = skillName.trim().toLowerCase();
            ProficiencyLevel requiredLevel = trs.getMinimumProficiency();
            double weight = trs.getImportanceWeight();
            totalWeight += weight;

            if (studentSkills.containsKey(skillKey)) {
                StudentSkill ss = studentSkills.get(skillKey);
                ProficiencyLevel currentLevel = ss.getProficiencyLevel();

                if (currentLevel.getNumericRank() >= requiredLevel.getNumericRank()) {
                    matched.add(new SkillGapAnalysisDto.SkillItemDto(
                            skillName, trs.getSkill().getCategory(), currentLevel, requiredLevel, weight, trs.isCore()
                    ));
                    accumulatedProficiency += weight * 1.0;
                } else {
                    weak.add(new SkillGapAnalysisDto.SkillItemDto(
                            skillName, trs.getSkill().getCategory(), currentLevel, requiredLevel, weight, trs.isCore()
                    ));
                    // Partial credit proportional to current proficiency tier
                    double ratio = (double) currentLevel.getNumericRank() / requiredLevel.getNumericRank();
                    accumulatedProficiency += weight * ratio * 0.75;
                }
            } else {
                missing.add(new SkillGapAnalysisDto.SkillItemDto(
                        skillName, trs.getSkill().getCategory(), null, requiredLevel, weight, trs.isCore()
                ));
            }
        }

        analysis.setMatchedSkills(matched);
        analysis.setWeakSkills(weak);
        analysis.setMissingSkills(missing);
        analysis.setSatisfiedSkillsCount(matched.size());

        double matchPercentage = totalWeight > 0 ? (accumulatedProficiency / totalWeight) * 100.0 : 0.0;
        analysis.setMatchPercentage(Math.round(matchPercentage * 10.0) / 10.0);

        return analysis;
    }
}
