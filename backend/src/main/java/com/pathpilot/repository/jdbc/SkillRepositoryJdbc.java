package com.pathpilot.repository.jdbc;

import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.ProficiencyLevel;
import com.pathpilot.model.Skill;
import com.pathpilot.model.TargetRoleSkill;
import com.pathpilot.repository.SkillRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of SkillRepository executing explicit SQL queries.
 */
@Repository
public class SkillRepositoryJdbc implements SkillRepository {

    private final JdbcTemplate jdbcTemplate;

    public SkillRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Skill> skillRowMapper = (rs, rowNum) -> new Skill(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("domain"),
            rs.getString("description")
    );

    @Override
    public Optional<Skill> findById(Long id) {
        String sql = "SELECT id, name, category, domain, description FROM skills WHERE id = ?";
        try {
            Skill skill = jdbcTemplate.queryForObject(sql, skillRowMapper, id);
            return Optional.ofNullable(skill);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching skill by id: " + id, e);
        }
    }

    @Override
    public Optional<Skill> findByName(String name) {
        String sql = "SELECT id, name, category, domain, description FROM skills WHERE LOWER(name) = LOWER(?)";
        try {
            Skill skill = jdbcTemplate.queryForObject(sql, skillRowMapper, name.trim());
            return Optional.ofNullable(skill);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching skill by name: " + name, e);
        }
    }

    @Override
    public List<Skill> findAll() {
        String sql = "SELECT id, name, category, domain, description FROM skills ORDER BY name ASC";
        try {
            return jdbcTemplate.query(sql, skillRowMapper);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching all skills", e);
        }
    }

    @Override
    public Skill save(Skill skill) {
        String sql = "INSERT INTO skills (name, category, domain, description) VALUES (?, ?, ?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, skill.getName());
                ps.setString(2, skill.getCategory());
                ps.setString(3, skill.getDomain());
                ps.setString(4, skill.getDescription());
                return ps;
            }, keyHolder);

            Long key = com.pathpilot.util.JdbcUtils.extractGeneratedId(keyHolder);
            if (key != null) {
                skill.setId(key);
            }
            return skill;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error saving skill: " + skill.getName(), e);
        }
    }

    @Override
    public List<TargetRoleSkill> findRoleSkills(String roleName) {
        String sql = "SELECT trs.id, trs.role_name, trs.minimum_proficiency, trs.importance_weight, trs.is_core, " +
                     "s.id AS skill_id, s.name AS skill_name, s.category, s.domain, s.description " +
                     "FROM target_role_skills trs " +
                     "JOIN skills s ON trs.skill_id = s.id " +
                     "WHERE LOWER(trs.role_name) = LOWER(?) " +
                     "ORDER BY trs.importance_weight DESC, trs.is_core DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Skill skill = new Skill(
                        rs.getLong("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("category"),
                        rs.getString("domain"),
                        rs.getString("description")
                );
                return new TargetRoleSkill(
                        rs.getLong("id"),
                        rs.getString("role_name"),
                        skill,
                        ProficiencyLevel.fromString(rs.getString("minimum_proficiency")),
                        rs.getDouble("importance_weight"),
                        rs.getBoolean("is_core")
                );
            }, roleName.trim());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error querying role skills for role: " + roleName, e);
        }
    }

    @Override
    public List<String> findAllTargetRoles() {
        String sql = "SELECT DISTINCT role_name FROM target_role_skills ORDER BY role_name ASC";
        try {
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching target roles", e);
        }
    }
}
