package com.pathpilot.service.impl;

import com.pathpilot.dto.StudentProfileUpdateDto;
import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.dto.StudentSkillInputDto;
import com.pathpilot.exception.DuplicateResourceException;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.Skill;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.StudentSkill;
import com.pathpilot.repository.SkillRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;

    public StudentServiceImpl(StudentRepository studentRepository, SkillRepository skillRepository) {
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional
    public StudentProfile registerStudent(StudentRegistrationDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new InvalidRecommendationRequestException("Student email must not be blank");
        }
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new InvalidRecommendationRequestException("Student full name must not be blank");
        }
        if (dto.getTargetRole() == null || dto.getTargetRole().isBlank()) {
            throw new InvalidRecommendationRequestException("Target role must not be blank");
        }

        // Check for duplicate email
        if (studentRepository.findByEmail(dto.getEmail().trim()).isPresent()) {
            throw new DuplicateResourceException("Student", "email", dto.getEmail());
        }

        StudentProfile student = new StudentProfile(
                null,
                dto.getFullName().trim(),
                dto.getEmail().trim(),
                dto.getTargetRole().trim(),
                dto.getWeeklyHoursAvailable(),
                dto.getMaxBudget(),
                dto.getPreferredDifficulty(),
                dto.getBio()
        );

        // Map initial skills
        List<StudentSkill> skills = new ArrayList<>();
        if (dto.getSkills() != null) {
            for (StudentSkillInputDto sInput : dto.getSkills()) {
                Optional<Skill> skillOpt = Optional.empty();
                if (sInput.getSkillId() != null) {
                    skillOpt = skillRepository.findById(sInput.getSkillId());
                } else if (sInput.getSkillName() != null) {
                    skillOpt = skillRepository.findByName(sInput.getSkillName());
                }

                skillOpt.ifPresent(skill -> skills.add(new StudentSkill(
                        null, skill, sInput.getProficiencyLevel(), sInput.getYearsExperience(), true
                )));
            }
        }
        student.setSkills(skills);

        return studentRepository.save(student);
    }

    @Override
    public StudentProfile getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    @Override
    public StudentProfile getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student email", email));
    }

    @Override
    @Transactional
    public StudentProfile updateStudentProfile(Long id, StudentProfileUpdateDto dto) {
        StudentProfile student = getStudentById(id);

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            student.setFullName(dto.getFullName().trim());
        }
        if (dto.getTargetRole() != null && !dto.getTargetRole().isBlank()) {
            student.setTargetRole(dto.getTargetRole().trim());
        }
        if (dto.getWeeklyHoursAvailable() > 0) {
            student.setWeeklyHoursAvailable(dto.getWeeklyHoursAvailable());
        }
        if (dto.getMaxBudget() != null) {
            student.setMaxBudget(dto.getMaxBudget());
        }
        if (dto.getPreferredDifficulty() != null) {
            student.setPreferredDifficulty(dto.getPreferredDifficulty());
        }
        if (dto.getBio() != null) {
            student.setBio(dto.getBio());
        }

        studentRepository.update(student);
        return student;
    }

    @Override
    public List<StudentProfile> getAllStudents() {
        return studentRepository.findAll();
    }
}
