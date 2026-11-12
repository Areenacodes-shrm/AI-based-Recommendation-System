package com.pathpilot.repository.jdbc;

import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.FeedbackAction;
import com.pathpilot.model.UserFeedback;
import com.pathpilot.repository.FeedbackRepository;
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
import java.sql.Types;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FeedbackRepository.
 */
@Repository
public class FeedbackRepositoryJdbc implements FeedbackRepository {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserFeedback> feedbackRowMapper = (rs, rowNum) -> {
        UserFeedback fb = new UserFeedback();
        fb.setId(rs.getLong("id"));
        fb.setStudentId(rs.getLong("student_id"));
        fb.setOpportunityId(rs.getLong("opportunity_id"));
        fb.setActionType(FeedbackAction.fromString(rs.getString("action_type")));

        int rating = rs.getInt("rating_value");
        if (!rs.wasNull()) {
            fb.setRatingValue(rating);
        }
        fb.setNotes(rs.getString("notes"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) fb.setCreatedAt(ts.toLocalDateTime());

        return fb;
    };

    @Override
    public UserFeedback save(UserFeedback feedback) {
        String sql = "INSERT INTO user_feedback (student_id, opportunity_id, action_type, rating_value, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, feedback.getStudentId());
                ps.setLong(2, feedback.getOpportunityId());
                ps.setString(3, feedback.getActionType().name());
                if (feedback.getRatingValue() != null) {
                    ps.setInt(4, feedback.getRatingValue());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setString(5, feedback.getNotes());
                return ps;
            }, keyHolder);

            Long key = JdbcUtils.extractGeneratedId(keyHolder);
            if (key != null) {
                feedback.setId(key);
            }
            return feedback;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error saving user feedback", e);
        }
    }

    @Override
    public List<UserFeedback> findByStudentId(Long studentId) {
        String sql = "SELECT id, student_id, opportunity_id, action_type, rating_value, notes, created_at " +
                     "FROM user_feedback WHERE student_id = ? ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, feedbackRowMapper, studentId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching feedback for student: " + studentId, e);
        }
    }

    @Override
    public List<UserFeedback> findByOpportunityId(Long opportunityId) {
        String sql = "SELECT id, student_id, opportunity_id, action_type, rating_value, notes, created_at " +
                     "FROM user_feedback WHERE opportunity_id = ? ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, feedbackRowMapper, opportunityId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching feedback for opportunity: " + opportunityId, e);
        }
    }

    @Override
    public Optional<UserFeedback> findByStudentAndOpportunity(Long studentId, Long opportunityId) {
        String sql = "SELECT id, student_id, opportunity_id, action_type, rating_value, notes, created_at " +
                     "FROM user_feedback WHERE student_id = ? AND opportunity_id = ? ORDER BY created_at DESC LIMIT 1";
        try {
            UserFeedback fb = jdbcTemplate.queryForObject(sql, feedbackRowMapper, studentId, opportunityId);
            return Optional.ofNullable(fb);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching feedback", e);
        }
    }

    @Override
    public Optional<UserFeedback> findByStudentOpportunityAndAction(Long studentId, Long opportunityId, FeedbackAction actionType) {
        String sql = "SELECT id, student_id, opportunity_id, action_type, rating_value, notes, created_at " +
                     "FROM user_feedback WHERE student_id = ? AND opportunity_id = ? AND action_type = ? ORDER BY created_at DESC LIMIT 1";
        try {
            UserFeedback fb = jdbcTemplate.queryForObject(sql, feedbackRowMapper, studentId, opportunityId, actionType.name());
            return Optional.ofNullable(fb);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error checking feedback action", e);
        }
    }

    @Override
    public List<UserFeedback> findAll() {
        String sql = "SELECT id, student_id, opportunity_id, action_type, rating_value, notes, created_at " +
                     "FROM user_feedback ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, feedbackRowMapper);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error querying all feedback", e);
        }
    }
}
