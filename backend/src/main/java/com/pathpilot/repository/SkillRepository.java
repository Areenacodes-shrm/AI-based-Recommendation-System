package com.pathpilot.repository;

import com.pathpilot.model.Skill;
import com.pathpilot.model.TargetRoleSkill;

import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Skill catalog and Role-Skill benchmark queries.
 */
public interface SkillRepository {
    Optional<Skill> findById(Long id);
    Optional<Skill> findByName(String name);
    List<Skill> findAll();
    Skill save(Skill skill);
    List<TargetRoleSkill> findRoleSkills(String roleName);
    List<String> findAllTargetRoles();
}
