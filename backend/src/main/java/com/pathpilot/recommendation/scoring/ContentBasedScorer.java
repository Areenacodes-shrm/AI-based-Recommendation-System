package com.pathpilot.recommendation.scoring;

import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.RecommendationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Evaluates semantic content overlap between student interests, bio, target role
 * and opportunity title, description, and syllabus.
 */
@Component
public class ContentBasedScorer implements RecommendationScorer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "in", "on", "at", "to", "for", "with",
            "is", "are", "of", "by", "from", "as", "into", "through", "using", "master"
    );

    @Override
    public double score(StudentProfile student, Opportunity opportunity, RecommendationContext context) {
        if (student == null || opportunity == null) return 0.0;

        // Extract student token set
        Set<String> studentTokens = extractTokens(student.getTargetRole() + " " + (student.getBio() != null ? student.getBio() : ""));

        // Extract opportunity token set
        Set<String> oppTokens = extractTokens(opportunity.getTitle() + " " + opportunity.getDescription() + " " + opportunity.getProvider());

        if (studentTokens.isEmpty() || oppTokens.isEmpty()) {
            return 0.35; // Neutral baseline
        }

        // Compute Jaccard coefficient on significant keywords
        Set<String> intersection = new HashSet<>(studentTokens);
        intersection.retainAll(oppTokens);

        Set<String> union = new HashSet<>(studentTokens);
        union.addAll(oppTokens);

        double jaccard = union.isEmpty() ? 0.0 : ((double) intersection.size() / union.size());

        // Target role token match bonus: full phrase match or individual role keyword overlap
        String targetRoleLower = student.getTargetRole().toLowerCase();
        String oppFullText = (opportunity.getTitle() + " " + opportunity.getDescription() + " " + opportunity.getProvider()).toLowerCase();
        double roleBoost = 0.0;
        if (oppFullText.contains(targetRoleLower)) {
            roleBoost = 0.30;
        } else {
            Set<String> roleTokens = extractTokens(targetRoleLower);
            if (!roleTokens.isEmpty()) {
                long matchedRoleTokens = roleTokens.stream().filter(oppFullText::contains).count();
                roleBoost = 0.25 * ((double) matchedRoleTokens / roleTokens.size());
            }
        }

        // Bounded normalization [0.0, 1.0]
        return Math.min(1.0, (jaccard * 2.5) + roleBoost);
    }

    @Override
    public String getScorerName() {
        return "ContentBasedScorer";
    }

    private Set<String> extractTokens(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = clean.split("\\s+");
        Set<String> tokens = new HashSet<>();
        for (String w : words) {
            if (w.length() > 2 && !STOP_WORDS.contains(w)) {
                tokens.add(w);
            }
        }
        return tokens;
    }
}
