package com.pathpilot.model;

import java.util.Objects;

/**
 * Benchmark skill expectations for a career role.
 */
public class TargetRoleSkill {
    private Long id;
    private String roleName;
    private Skill skill;
    private ProficiencyLevel minimumProficiency;
    private double importanceWeight;
    private boolean core;

    public TargetRoleSkill() {}

    public TargetRoleSkill(Long id, String roleName, Skill skill, ProficiencyLevel minimumProficiency, double importanceWeight, boolean core) {
        this.id = id;
        this.roleName = roleName;
        this.skill = skill;
        this.minimumProficiency = minimumProficiency;
        this.importanceWeight = importanceWeight;
        this.core = core;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public ProficiencyLevel getMinimumProficiency() {
        return minimumProficiency;
    }

    public void setMinimumProficiency(ProficiencyLevel minimumProficiency) {
        this.minimumProficiency = minimumProficiency;
    }

    public double getImportanceWeight() {
        return importanceWeight;
    }

    public void setImportanceWeight(double importanceWeight) {
        this.importanceWeight = Math.max(0.0, Math.min(1.0, importanceWeight));
    }

    public boolean isCore() {
        return core;
    }

    public void setCore(boolean core) {
        this.core = core;
    }
}
