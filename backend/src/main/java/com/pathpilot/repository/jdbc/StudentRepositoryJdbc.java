package com.pathpilot.repository.jdbc;

import com.pathpilot.dto.DemoProfileDto;
import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.*;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.util.JdbcUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

/**
 * JDBC implementation of StudentRepository executing explicit parameterized SQL queries.
 */
@Repository
public class StudentRepositoryJdbc implements StudentRepository {

    private static final String BASE_COLUMNS =
            "id, full_name, email, password_hash, is_demo, target_role, weekly_hours_available, max_budget, preferred_difficulty, bio, created_at, updated_at";

    private final JdbcTemplate jdbcTemplate;

    public StudentRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StudentProfile> studentRowMapper = (rs, rowNum) -> {
        StudentProfile student = new StudentProfile();
        student.setId(rs.getLong("id"));
        student.setFullName(rs.getString("full_name"));
        student.setEmail(rs.getString("email"));
        student.setPasswordHash(rs.getString("password_hash"));
        student.setDemoAccount(rs.getBoolean("is_demo"));
        student.setTargetRole(rs.getString("target_role"));
        student.setWeeklyHoursAvailable(rs.getInt("weekly_hours_available"));
        student.setMaxBudget(rs.getBigDecimal("max_budget"));
        student.setPreferredDifficulty(DifficultyLevel.fromString(rs.getString("preferred_difficulty")));
        student.setBio(rs.getString("bio"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) student.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) student.setUpdatedAt(updatedAt.toLocalDateTime());

        return student;
    };

    @Override
    public Optional<StudentProfile> findById(Long id) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM students WHERE id = ?";
        try {
            StudentProfile student = jdbcTemplate.queryForObject(sql, studentRowMapper, id);
            if (student != null) {
                populateStudentSkills(student);
                return Optional.of(student);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to find student by ID: " + id, e);
        }
    }

    @Override
    public Optional<StudentProfile> findByEmail(String email) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM students WHERE LOWER(email) = LOWER(?)";
        try {
            StudentProfile student = jdbcTemplate.queryForObject(sql, studentRowMapper, email);
            if (student != null) {
                populateStudentSkills(student);
                return Optional.of(student);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to find student by email", e);
        }
    }

    @Override
    @Transactional
    public StudentProfile save(StudentProfile student) {
        String sql = "INSERT INTO students (full_name, email, password_hash, is_demo, target_role, weekly_hours_available, max_budget, preferred_difficulty, bio) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, student.getFullName());
                ps.setString(2, student.getEmail());
                ps.setString(3, student.getPasswordHash() != null ? student.getPasswordHash() : "");
                ps.setBoolean(4, student.isDemoAccount());
                ps.setString(5, student.getTargetRole());
                ps.setInt(6, student.getWeeklyHoursAvailable());
                ps.setBigDecimal(7, student.getMaxBudget());
                ps.setString(8, student.getPreferredDifficulty().name());
                ps.setString(9, student.getBio());
                return ps;
            }, keyHolder);

            Long generatedId = JdbcUtils.extractGeneratedId(keyHolder);
            if (generatedId != null) {
                student.setId(generatedId);
            }

            if (!student.getSkillsMap().isEmpty()) {
                saveStudentSkills(student.getId(), student.getSkillsList());
            }

            return student;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to save student profile", e);
        }
    }

    @Override
    @Transactional
    public void update(StudentProfile student) {
        String sql = "UPDATE students SET full_name = ?, target_role = ?, weekly_hours_available = ?, " +
                     "max_budget = ?, preferred_difficulty = ?, bio = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            jdbcTemplate.update(sql,
                    student.getFullName(),
                    student.getTargetRole(),
                    student.getWeeklyHoursAvailable(),
                    student.getMaxBudget(),
                    student.getPreferredDifficulty().name(),
                    student.getBio(),
                    student.getId());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update student profile: " + student.getId(), e);
        }
    }

    @Override
    @Transactional
    public void saveStudentSkills(Long studentId, Collection<StudentSkill> skills) {
        String deleteSql = "DELETE FROM student_skills WHERE student_id = ?";
        String insertSql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level, years_experience, is_verified) " +
                           "VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(deleteSql, studentId);
            if (skills != null && !skills.isEmpty()) {
                List<Object[]> batchArgs = new ArrayList<>();
                for (StudentSkill ss : skills) {
                    batchArgs.add(new Object[]{
                            studentId,
                            ss.getSkill().getId(),
                            ss.getProficiencyLevel().name(),
                            ss.getYearsExperience(),
                            ss.isVerified()
                    });
                }
                jdbcTemplate.batchUpdate(insertSql, batchArgs);
            }
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to save skills for student: " + studentId, e);
        }
    }

    @Override
    public List<StudentSkill> findSkillsByStudentId(Long studentId) {
        String sql = "SELECT ss.student_id, ss.skill_id, ss.proficiency_level, ss.years_experience, ss.is_verified, " +
                     "s.name AS skill_name, s.category, s.domain, s.description " +
                     "FROM student_skills ss " +
                     "JOIN skills s ON ss.skill_id = s.id " +
                     "WHERE ss.student_id = ?";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Skill skill = new Skill(
                        rs.getLong("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("category"),
                        rs.getString("domain"),
                        rs.getString("description")
                );
                return new StudentSkill(
                        rs.getLong("student_id"),
                        skill,
                        ProficiencyLevel.fromString(rs.getString("proficiency_level")),
                        rs.getDouble("years_experience"),
                        rs.getBoolean("is_verified")
                );
            }, studentId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve skills for student: " + studentId, e);
        }
    }

    @Override
    public List<StudentProfile> findAll() {
        String sql = "SELECT " + BASE_COLUMNS + " FROM students ORDER BY id ASC";
        try {
            List<StudentProfile> students = jdbcTemplate.query(sql, studentRowMapper);
            for (StudentProfile s : students) {
                populateStudentSkills(s);
            }
            return students;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to fetch students list", e);
        }
    }

    @Override
    public List<DemoProfileDto> findDemoProfiles() {
        String sql = "SELECT id, full_name, target_role FROM students WHERE is_demo = TRUE ORDER BY id ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new DemoProfileDto(
                    rs.getLong("id"),
                    rs.getString("full_name"),
                    rs.getString("target_role")
            ));
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to fetch demo student profiles", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try {
            return jdbcTemplate.update(sql, id) > 0;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete student with id: " + id, e);
        }
    }

    private void populateStudentSkills(StudentProfile student) {
        List<StudentSkill> skills = findSkillsByStudentId(student.getId());
        student.setSkills(skills);
    }
}
