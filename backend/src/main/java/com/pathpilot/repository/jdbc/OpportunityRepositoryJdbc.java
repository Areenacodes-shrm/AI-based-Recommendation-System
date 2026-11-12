package com.pathpilot.repository.jdbc;

import com.pathpilot.exception.DatabaseOperationException;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.*;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.util.JdbcUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

/**
 * JDBC implementation of OpportunityRepository demonstrating Polymorphic Object Relational Mapping.
 * Correctly reconstructs specialized subclasses (CourseOpportunity, ProjectOpportunity,
 * InternshipOpportunity, CertificationOpportunity) from relational tables.
 */
@Repository
public class OpportunityRepositoryJdbc implements OpportunityRepository {

    private final JdbcTemplate jdbcTemplate;

    public OpportunityRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Opportunity> opportunityRowMapper = (rs, rowNum) -> {
        String oppTypeStr = rs.getString("opportunity_type");
        OpportunityType oppType = OpportunityType.valueOf(oppTypeStr);

        Opportunity opp;
        switch (oppType) {
            case COURSE -> {
                CourseOpportunity course = new CourseOpportunity();
                course.setPlatform(rs.getString("platform"));
                course.setSyllabusSummary(rs.getString("syllabus_summary"));
                course.setCertificateIncluded(rs.getBoolean("certificate_included"));
                course.setDurationWeeks(rs.getInt("duration_weeks"));
                opp = course;
            }
            case PROJECT -> {
                ProjectOpportunity proj = new ProjectOpportunity();
                proj.setRepositoryTemplateUrl(rs.getString("repository_template_url"));
                proj.setTechStackFromCsv(rs.getString("tech_stack_csv"));
                proj.setProjectTier(ProjectTier.fromString(rs.getString("project_tier")));
                proj.setDeliverableDescription(rs.getString("deliverable_description"));
                opp = proj;
            }
            case INTERNSHIP -> {
                InternshipOpportunity intern = new InternshipOpportunity();
                intern.setCompanyName(rs.getString("company_name"));
                intern.setMonthlyStipend(rs.getBigDecimal("monthly_stipend"));
                intern.setLocationType(LocationType.fromString(rs.getString("location_type")));
                intern.setDurationMonths(rs.getInt("duration_months"));
                intern.setApplicationDeadline(rs.getString("application_deadline"));
                opp = intern;
            }
            case CERTIFICATION -> {
                CertificationOpportunity cert = new CertificationOpportunity();
                cert.setIssuingOrganization(rs.getString("issuing_organization"));
                cert.setExamCode(rs.getString("exam_code"));
                cert.setValidityYears(rs.getInt("validity_years"));
                cert.setExamFormat(rs.getString("exam_format"));
                opp = cert;
            }
            default -> throw new IllegalStateException("Unknown opportunity type: " + oppTypeStr);
        }

        // Populate common parent fields
        opp.setId(rs.getLong("id"));
        opp.setTitle(rs.getString("title"));
        opp.setDescription(rs.getString("description"));
        opp.setDifficultyLevel(DifficultyLevel.fromString(rs.getString("difficulty_level")));
        opp.setEstimatedHours(rs.getDouble("estimated_hours"));
        opp.setCost(rs.getBigDecimal("cost"));
        opp.setProvider(rs.getString("provider"));
        opp.setUrl(rs.getString("url"));
        opp.setRating(rs.getDouble("rating"));
        opp.setRatingCount(rs.getInt("rating_count"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) opp.setCreatedAt(createdAt.toLocalDateTime());

        return opp;
    };

    private static final String BASE_SELECT_SQL =
            "SELECT o.id, o.title, o.description, o.opportunity_type, o.difficulty_level, o.estimated_hours, " +
            "       o.cost, o.provider, o.url, o.rating, o.rating_count, o.created_at, " +
            "       c.platform, c.syllabus_summary, c.certificate_included, c.duration_weeks, " +
            "       p.repository_template_url, p.tech_stack_csv, p.project_tier, p.deliverable_description, " +
            "       i.company_name, i.monthly_stipend, i.location_type, i.duration_months, i.application_deadline, " +
            "       cr.issuing_organization, cr.exam_code, cr.validity_years, cr.exam_format " +
            "FROM opportunities o " +
            "LEFT JOIN course_details c ON o.id = c.opportunity_id " +
            "LEFT JOIN project_details p ON o.id = p.opportunity_id " +
            "LEFT JOIN internship_details i ON o.id = i.opportunity_id " +
            "LEFT JOIN certification_details cr ON o.id = cr.opportunity_id ";

    @Override
    public Optional<Opportunity> findById(Long id) {
        String sql = BASE_SELECT_SQL + "WHERE o.id = ?";
        try {
            Opportunity opp = jdbcTemplate.queryForObject(sql, opportunityRowMapper, id);
            if (opp != null) {
                populateOpportunitySkills(opp);
                return Optional.of(opp);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching opportunity with id: " + id, e);
        }
    }

    @Override
    public List<Opportunity> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL).append("WHERE o.id IN (");
        for (int i = 0; i < validIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");

        try {
            List<Opportunity> opps = jdbcTemplate.query(sql.toString(), opportunityRowMapper, validIds.toArray());
            for (Opportunity opp : opps) {
                populateOpportunitySkills(opp);
            }
            return opps;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error fetching opportunities by IDs", e);
        }
    }

    @Override
    public List<Opportunity> findAll() {
        String sql = BASE_SELECT_SQL + "ORDER BY o.rating DESC, o.id ASC";
        try {
            List<Opportunity> opportunities = jdbcTemplate.query(sql, opportunityRowMapper);
            for (Opportunity opp : opportunities) {
                populateOpportunitySkills(opp);
            }
            return opportunities;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error querying all opportunities", e);
        }
    }

    @Override
    public List<Opportunity> search(String query, OpportunityType type, DifficultyLevel difficulty,
                                    BigDecimal maxCost, int limit, int offset) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(o.title) LIKE ? OR LOWER(o.description) LIKE ? OR LOWER(o.provider) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }

        if (type != null) {
            sql.append("AND o.opportunity_type = ? ");
            params.add(type.name());
        }

        if (difficulty != null) {
            sql.append("AND o.difficulty_level = ? ");
            params.add(difficulty.name());
        }

        if (maxCost != null && maxCost.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append("AND o.cost <= ? ");
            params.add(maxCost);
        }

        sql.append("ORDER BY o.rating DESC, o.rating_count DESC LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        try {
            List<Opportunity> list = jdbcTemplate.query(sql.toString(), opportunityRowMapper, params.toArray());
            for (Opportunity opp : list) {
                populateOpportunitySkills(opp);
            }
            return list;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error during opportunity search", e);
        }
    }

    @Override
    public List<Opportunity> findCandidatesByRoleAndInterests(String roleName, Collection<String> skillNames, int limit) {
        // Retrieve candidate opportunities that either teach or require target role skills or user skills
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL);
        sql.append("WHERE o.id IN ( ");
        sql.append("    SELECT DISTINCT os.opportunity_id FROM opportunity_skills os ");
        sql.append("    JOIN skills s ON os.skill_id = s.id ");
        sql.append("    LEFT JOIN target_role_skills trs ON trs.skill_id = s.id ");
        sql.append("    WHERE LOWER(trs.role_name) = LOWER(?) ");

        List<Object> params = new ArrayList<>();
        params.add(roleName != null ? roleName.trim() : "");

        if (skillNames != null && !skillNames.isEmpty()) {
            sql.append("    OR LOWER(s.name) IN (");
            int i = 0;
            for (String sName : skillNames) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(sName.trim().toLowerCase());
                i++;
            }
            sql.append(") ");
        }

        sql.append(") ORDER BY o.rating DESC LIMIT ?");
        params.add(Math.max(10, limit));

        try {
            List<Opportunity> candidates = new ArrayList<>(
                    jdbcTemplate.query(sql.toString(), opportunityRowMapper, params.toArray()));
            for (Opportunity opp : candidates) {
                populateOpportunitySkills(opp);
            }
            // Backfill with top-rated catalog items during cold-start or niche roles
            if (candidates.size() < 6) {
                List<Opportunity> topRated = findAll();
                Set<Long> existingIds = new HashSet<>();
                for (Opportunity o : candidates) existingIds.add(o.getId());
                for (Opportunity o : topRated) {
                    if (existingIds.add(o.getId())) {
                        candidates.add(o);
                    }
                    if (candidates.size() >= limit) break;
                }
            }
            return candidates;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error retrieving candidates for role: " + roleName, e);
        }
    }

    @Override
    @Transactional
    public Opportunity save(Opportunity opp) {
        String baseSql = "INSERT INTO opportunities (title, description, opportunity_type, difficulty_level, " +
                         "estimated_hours, cost, provider, url, rating, rating_count) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(baseSql, new String[]{"id"});
                ps.setString(1, opp.getTitle());
                ps.setString(2, opp.getDescription());
                ps.setString(3, opp.getOpportunityType().name());
                ps.setString(4, opp.getDifficultyLevel().name());
                ps.setDouble(5, opp.getEstimatedHours());
                ps.setBigDecimal(6, opp.getCost());
                ps.setString(7, opp.getProvider());
                ps.setString(8, opp.getUrl());
                ps.setDouble(9, opp.getRating());
                ps.setInt(10, opp.getRatingCount());
                return ps;
            }, keyHolder);

            Long idKey = JdbcUtils.extractGeneratedId(keyHolder);
            if (idKey != null) {
                opp.setId(idKey);
            }

            // Save polymorphic subtype details
            saveSubtypeDetails(opp);

            return opp;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error saving opportunity: " + opp.getTitle(), e);
        }
    }

    private void saveSubtypeDetails(Opportunity opp) {
        Long oppId = opp.getId();
        if (opp instanceof CourseOpportunity course) {
            String sql = "INSERT INTO course_details (opportunity_id, platform, syllabus_summary, certificate_included, duration_weeks) " +
                         "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, oppId, course.getPlatform(), course.getSyllabusSummary(),
                    course.isCertificateIncluded(), course.getDurationWeeks());
        } else if (opp instanceof ProjectOpportunity proj) {
            String sql = "INSERT INTO project_details (opportunity_id, repository_template_url, tech_stack_csv, project_tier, deliverable_description) " +
                         "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, oppId, proj.getRepositoryTemplateUrl(), proj.getTechStackCsv(),
                    proj.getProjectTier().name(), proj.getDeliverableDescription());
        } else if (opp instanceof InternshipOpportunity intern) {
            String sql = "INSERT INTO internship_details (opportunity_id, company_name, monthly_stipend, location_type, duration_months, application_deadline) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, oppId, intern.getCompanyName(), intern.getMonthlyStipend(),
                    intern.getLocationType().name(), intern.getDurationMonths(), intern.getApplicationDeadline());
        } else if (opp instanceof CertificationOpportunity cert) {
            String sql = "INSERT INTO certification_details (opportunity_id, issuing_organization, exam_code, validity_years, exam_format) " +
                         "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, oppId, cert.getIssuingOrganization(), cert.getExamCode(),
                    cert.getValidityYears(), cert.getExamFormat());
        }
    }

    @Override
    public void attachSkills(Long opportunityId, Long skillId, String relationshipType, ProficiencyLevel targetProficiency) {
        String sql = "INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) " +
                     "VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, opportunityId, skillId, relationshipType, targetProficiency.name());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error attaching skill to opportunity", e);
        }
    }

    private void populateOpportunitySkills(Opportunity opp) {
        if (opp == null || opp.getId() == null) return;
        String sql = "SELECT os.relationship_type, s.id, s.name, s.category, s.domain, s.description " +
                     "FROM opportunity_skills os " +
                     "JOIN skills s ON os.skill_id = s.id " +
                     "WHERE os.opportunity_id = ?";
        try {
            List<Skill> gained = new ArrayList<>();
            List<Skill> prereqs = new ArrayList<>();

            jdbcTemplate.query(sql, (rs) -> {
                String rel = rs.getString("relationship_type");
                Skill skill = new Skill(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("domain"),
                        rs.getString("description")
                );
                if ("GAINED".equalsIgnoreCase(rel)) {
                    gained.add(skill);
                } else if ("PREREQUISITE".equalsIgnoreCase(rel)) {
                    prereqs.add(skill);
                }
            }, opp.getId());

            opp.setSkillsGained(gained);
            opp.setPrerequisiteSkills(prereqs);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error populating opportunity skills: " + opp.getId(), e);
        }
    }
}
