package com.pathpilot.repository.jdbc;

import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.model.PlanStatus;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.LearningPlanRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.util.JdbcUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of LearningPlanRepository managing roadmap sequence and status.
 */
@Repository
public class LearningPlanRepositoryJdbc implements LearningPlanRepository {

    private final JdbcTemplate jdbcTemplate;
    private final OpportunityRepository opportunityRepository;

    public LearningPlanRepositoryJdbc(JdbcTemplate jdbcTemplate, OpportunityRepository opportunityRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.opportunityRepository = opportunityRepository;
    }

    private final RowMapper<LearningPlanItem> planRowMapper = (rs, rowNum) -> {
        LearningPlanItem item = new LearningPlanItem();
        item.setId(rs.getLong("id"));
        item.setStudentId(rs.getLong("student_id"));
        item.setOpportunityId(rs.getLong("opportunity_id"));
        item.setSequenceOrder(rs.getInt("sequence_order"));
        item.setStatus(PlanStatus.fromString(rs.getString("status")));
        item.setTargetCompletionWeeks(rs.getInt("target_completion_weeks"));
        item.setPrerequisiteNote(rs.getString("prerequisite_note"));

        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) item.setCreatedAt(cAt.toLocalDateTime());
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) item.setUpdatedAt(uAt.toLocalDateTime());

        return item;
    };

    @Override
    public LearningPlanItem save(LearningPlanItem item) {
        String sql = "INSERT INTO learning_plans (student_id, opportunity_id, sequence_order, status, " +
                     "target_completion_weeks, prerequisite_note) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, item.getStudentId());
                ps.setLong(2, item.getOpportunityId());
                ps.setInt(3, item.getSequenceOrder());
                ps.setString(4, item.getStatus().name());
                ps.setInt(5, item.getTargetCompletionWeeks());
                ps.setString(6, item.getPrerequisiteNote());
                return ps;
            }, keyHolder);

            Long key = JdbcUtils.extractGeneratedId(keyHolder);
            if (key != null) {
                item.setId(key);
            }
            return item;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error saving learning plan item", e);
        }
    }

    @Override
    public List<LearningPlanItem> findByStudentId(Long studentId) {
        String sql = "SELECT id, student_id, opportunity_id, sequence_order, status, " +
                     "target_completion_weeks, prerequisite_note, created_at, updated_at " +
                     "FROM learning_plans WHERE student_id = ? ORDER BY sequence_order ASC, id ASC";
        try {
            List<LearningPlanItem> items = jdbcTemplate.query(sql, planRowMapper, studentId);
            for (LearningPlanItem item : items) {
                Optional<Opportunity> oppOpt = opportunityRepository.findById(item.getOpportunityId());
                oppOpt.ifPresent(item::setOpportunity);
            }
            return items;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error retrieving learning plan for student: " + studentId, e);
        }
    }

    @Override
    public Optional<LearningPlanItem> findById(Long id) {
        String sql = "SELECT id, student_id, opportunity_id, sequence_order, status, " +
                     "target_completion_weeks, prerequisite_note, created_at, updated_at " +
                     "FROM learning_plans WHERE id = ?";
        try {
            LearningPlanItem item = jdbcTemplate.queryForObject(sql, planRowMapper, id);
            if (item != null) {
                opportunityRepository.findById(item.getOpportunityId()).ifPresent(item::setOpportunity);
                return Optional.of(item);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching learning plan item: " + id, e);
        }
    }

    @Override
    public Optional<LearningPlanItem> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId) {
        String sql = "SELECT id, student_id, opportunity_id, sequence_order, status, " +
                     "target_completion_weeks, prerequisite_note, created_at, updated_at " +
                     "FROM learning_plans WHERE student_id = ? AND opportunity_id = ? LIMIT 1";
        try {
            LearningPlanItem item = jdbcTemplate.queryForObject(sql, planRowMapper, studentId, opportunityId);
            if (item != null) {
                opportunityRepository.findById(item.getOpportunityId()).ifPresent(item::setOpportunity);
                return Optional.of(item);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error checking learning plan entry", e);
        }
    }

    @Override
    public void updateStatus(Long id, PlanStatus status, Integer targetCompletionWeeks) {
        StringBuilder sql = new StringBuilder("UPDATE learning_plans SET updated_at = CURRENT_TIMESTAMP");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(", status = ?");
            params.add(status.name());
        }
        if (targetCompletionWeeks != null && targetCompletionWeeks > 0) {
            sql.append(", target_completion_weeks = ?");
            params.add(targetCompletionWeeks);
        }

        sql.append(" WHERE id = ?");
        params.add(id);

        try {
            jdbcTemplate.update(sql.toString(), params.toArray());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error updating learning plan status for item: " + id, e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM learning_plans WHERE id = ?";
        try {
            return jdbcTemplate.update(sql, id) > 0;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error deleting learning plan item: " + id, e);
        }
    }

    @Override
    public void clearForStudent(Long studentId) {
        String sql = "DELETE FROM learning_plans WHERE student_id = ?";
        try {
            jdbcTemplate.update(sql, studentId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error clearing learning plans for student: " + studentId, e);
        }
    }
}
