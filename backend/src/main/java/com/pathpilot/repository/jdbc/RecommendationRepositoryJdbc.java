package com.pathpilot.repository.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.ExplanationData;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.RecommendationRepository;
import com.pathpilot.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of RecommendationRepository.
 */
@Repository
public class RecommendationRepositoryJdbc implements RecommendationRepository {

    private static final Logger log = LoggerFactory.getLogger(RecommendationRepositoryJdbc.class);

    private final JdbcTemplate jdbcTemplate;
    private final OpportunityRepository opportunityRepository;
    private final ObjectMapper objectMapper;

    public RecommendationRepositoryJdbc(JdbcTemplate jdbcTemplate,
                                        OpportunityRepository opportunityRepository,
                                        ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.opportunityRepository = opportunityRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Long saveRecommendation(Long studentId, String strategyUsed, List<RecommendationItem> items) {
        String recSql = "INSERT INTO recommendations (student_id, strategy_used) VALUES (?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(recSql, new String[]{"id"});
                ps.setLong(1, studentId);
                ps.setString(2, strategyUsed);
                return ps;
            }, keyHolder);

            Long recommendationId = JdbcUtils.extractGeneratedId(keyHolder);
            if (recommendationId == null) {
                throw new DatabaseOperationException("Failed to generate recommendation ID");
            }

            String itemSql = "INSERT INTO recommendation_items (recommendation_id, opportunity_id, ranking_score, " +
                             "content_score, skill_score, contextual_score, feedback_score, rank_order, explanation_json) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            List<Object[]> batchParams = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                RecommendationItem item = items.get(i);
                String explanationJson = "{}";
                try {
                    if (item.getExplanation() != null) {
                        explanationJson = objectMapper.writeValueAsString(item.getExplanation());
                    }
                } catch (JsonProcessingException ex) {
                    log.warn("Could not serialize explanation JSON for opportunity {}", item.getOpportunity().getId(), ex);
                }

                batchParams.add(new Object[]{
                        recommendationId,
                        item.getOpportunity().getId(),
                        item.getRankingScore(),
                        item.getContentScore(),
                        item.getSkillScore(),
                        item.getContextualScore(),
                        item.getFeedbackScore(),
                        i + 1,
                        explanationJson
                });
            }

            jdbcTemplate.batchUpdate(itemSql, batchParams);
            return recommendationId;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error saving recommendation history", e);
        }
    }

    @Override
    public List<RecommendationItem> findLatestByStudentId(Long studentId) {
        String latestRecSql = "SELECT id FROM recommendations WHERE student_id = ? ORDER BY generated_at DESC LIMIT 1";
        try {
            Long recId = jdbcTemplate.queryForObject(latestRecSql, Long.class, studentId);
            if (recId == null) return List.of();

            String itemsSql = "SELECT opportunity_id, ranking_score, content_score, skill_score, " +
                              "contextual_score, feedback_score, rank_order, explanation_json " +
                              "FROM recommendation_items WHERE recommendation_id = ? ORDER BY rank_order ASC";

            return jdbcTemplate.query(itemsSql, (rs, rowNum) -> {
                Long oppId = rs.getLong("opportunity_id");
                Optional<Opportunity> oppOpt = opportunityRepository.findById(oppId);
                if (oppOpt.isEmpty()) return null;

                RecommendationItem item = new RecommendationItem();
                item.setOpportunity(oppOpt.get());
                item.setRankingScore(rs.getDouble("ranking_score"));
                item.setContentScore(rs.getDouble("content_score"));
                item.setSkillScore(rs.getDouble("skill_score"));
                item.setContextualScore(rs.getDouble("contextual_score"));
                item.setFeedbackScore(rs.getDouble("feedback_score"));
                item.setRankOrder(rs.getInt("rank_order"));

                String json = rs.getString("explanation_json");
                try {
                    if (json != null && !json.isBlank()) {
                        item.setExplanation(objectMapper.readValue(json, ExplanationData.class));
                    }
                } catch (Exception ex) {
                    log.warn("Could not parse explanation JSON for opportunity {}", oppId, ex);
                }

                return item;
            }, recId).stream().filter(Objects::nonNull).toList();
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error querying latest recommendations for student: " + studentId, e);
        }
    }
}
