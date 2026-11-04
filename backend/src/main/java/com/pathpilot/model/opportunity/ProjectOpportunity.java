package com.pathpilot.model.opportunity;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.ProjectTier;
import com.pathpilot.model.StudentProfile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Specialized Opportunity subclass representing hands-on projects, portfolio builds, and open-source contributions.
 */
public class ProjectOpportunity extends Opportunity {
    private String repositoryTemplateUrl;
    private List<String> techStack = new ArrayList<>();
    private ProjectTier projectTier;
    private String deliverableDescription;

    public ProjectOpportunity() {
        super();
        this.projectTier = ProjectTier.GUIDED;
    }

    public ProjectOpportunity(Long id, String title, String description, DifficultyLevel difficultyLevel,
                              double estimatedHours, BigDecimal cost, String provider, String url,
                              String repositoryTemplateUrl, List<String> techStack,
                              ProjectTier projectTier, String deliverableDescription) {
        super(id, title, description, difficultyLevel, estimatedHours, cost, provider, url);
        this.repositoryTemplateUrl = repositoryTemplateUrl;
        this.techStack = techStack != null ? techStack : new ArrayList<>();
        this.projectTier = projectTier != null ? projectTier : ProjectTier.GUIDED;
        this.deliverableDescription = deliverableDescription;
    }

    @Override
    public OpportunityType getOpportunityType() {
        return OpportunityType.PROJECT;
    }

    @Override
    public double calculateDifficultyPenalty(StudentProfile student) {
        if (student == null || student.getPreferredDifficulty() == null) return 0.0;
        int diffDelta = getDifficultyLevel().getNumericRank() - student.getPreferredDifficulty().getNumericRank();
        // Practical projects require autonomous coding; higher penalty if student is beginner tackling advanced project
        if (diffDelta > 0) {
            return 0.20 * diffDelta;
        }
        return 0.0;
    }

    @Override
    public String getOpportunityBadge() {
        return "Project • " + (projectTier != null ? projectTier.name() : "GUIDED");
    }

    public String getRepositoryTemplateUrl() {
        return repositoryTemplateUrl;
    }

    public void setRepositoryTemplateUrl(String repositoryTemplateUrl) {
        this.repositoryTemplateUrl = repositoryTemplateUrl;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public void setTechStack(List<String> techStack) {
        this.techStack = techStack != null ? techStack : new ArrayList<>();
    }

    public void setTechStackFromCsv(String csv) {
        this.techStack.clear();
        if (csv != null && !csv.isBlank()) {
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.techStack::add);
        }
    }

    public String getTechStackCsv() {
        return String.join(",", techStack);
    }

    public ProjectTier getProjectTier() {
        return projectTier;
    }

    public void setProjectTier(ProjectTier projectTier) {
        this.projectTier = projectTier;
    }

    public String getDeliverableDescription() {
        return deliverableDescription;
    }

    public void setDeliverableDescription(String deliverableDescription) {
        this.deliverableDescription = deliverableDescription;
    }
}
