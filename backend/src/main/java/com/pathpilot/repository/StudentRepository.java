package com.pathpilot.repository;

import com.pathpilot.dto.DemoProfileDto;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.StudentSkill;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Student entity persistence operations.
 */
public interface StudentRepository {
    Optional<StudentProfile> findById(Long id);
    Optional<StudentProfile> findByEmail(String email);
    StudentProfile save(StudentProfile student);
    void update(StudentProfile student);
    void saveStudentSkills(Long studentId, Collection<StudentSkill> skills);
    List<StudentSkill> findSkillsByStudentId(Long studentId);
    List<StudentProfile> findAll();
    List<DemoProfileDto> findDemoProfiles();
    boolean deleteById(Long id);
}
