package com.pathpilot.repository;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.ProficiencyLevel;
import com.pathpilot.model.opportunity.Opportunity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for polymorphic Opportunity persistence and retrieval.
 */
public interface OpportunityRepository {
    Optional<Opportunity> findById(Long id);
    List<Opportunity> findByIds(Collection<Long> ids);
    List<Opportunity> findAll();
    List<Opportunity> search(String query, OpportunityType type, DifficultyLevel difficulty, BigDecimal maxCost, int limit, int offset);
    Opportunity save(Opportunity opportunity);
    void attachSkills(Long opportunityId, Long skillId, String relationshipType, ProficiencyLevel targetProficiency);
    List<Opportunity> findCandidatesByRoleAndInterests(String roleName, Collection<String> skillNames, int limit);
}
