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
import com.pathpilot.util.PasswordHasher;
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
        if (dto.getPassword() == null || dto.getPassword().isBlank() || dto.getPassword().length() < 8) {
            throw new InvalidRecommendationRequestException("Password must be at least 8 characters long");
        }

        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (studentRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateResourceException("Student", "email", normalizedEmail);
        }

        StudentProfile student = new StudentProfile(
                null,
                dto.getFullName().trim(),
                normalizedEmail,
                dto.getTargetRole().trim(),
                dto.getWeeklyHoursAvailable(),
                dto.getMaxBudget(),
                dto.getPreferredDifficulty(),
                dto.getBio()
        );
        student.setPasswordHash(PasswordHasher.hash(dto.getPassword()));
        student.setDemoAccount(false);

        List<StudentSkill> skills = resolveSkillsInput(dto.getSkills());
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
        if (dto.getWeeklyHoursAvailable() != null && dto.getWeeklyHoursAvailable() > 0) {
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

        if (dto.getSkills() != null) {
            List<StudentSkill> updatedSkills = resolveSkillsInput(dto.getSkills());
            studentRepository.saveStudentSkills(id, updatedSkills);
            student.setSkills(updatedSkills);
        }

        return getStudentById(id);
    }

    private List<StudentSkill> resolveSkillsInput(List<StudentSkillInputDto> skillInputs) {
        List<StudentSkill> skills = new ArrayList<>();
        if (skillInputs == null) {
            return skills;
        }
        for (StudentSkillInputDto sInput : skillInputs) {
            Optional<Skill> skillOpt;
            if (sInput.getSkillId() != null) {
                skillOpt = skillRepository.findById(sInput.getSkillId());
                if (skillOpt.isEmpty()) {
                    throw new InvalidRecommendationRequestException("Unknown skill ID: " + sInput.getSkillId());
                }
            } else if (sInput.getSkillName() != null && !sInput.getSkillName().isBlank()) {
                skillOpt = skillRepository.findByName(sInput.getSkillName());
                if (skillOpt.isEmpty()) {
                    throw new InvalidRecommendationRequestException("Unknown skill name: " + sInput.getSkillName());
                }
            } else {
                continue;
            }
            Skill resolved = skillOpt.get();
            skills.add(new StudentSkill(
                    null, resolved, sInput.getProficiencyLevel(), sInput.getYearsExperience(), true
            ));
        }
        return skills;
    }

    @Override
    public List<StudentProfile> getAllStudents() {
        return studentRepository.findAll();
    }
}
