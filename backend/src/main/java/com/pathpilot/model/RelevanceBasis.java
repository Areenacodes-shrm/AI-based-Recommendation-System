package com.pathpilot.model;

/**
 * Distinguishes the informational basis behind recommendation scores so users clearly
 * see whether results are driven by cold-start heuristics, verified profile skills,
 * or explicit historical feedback signals.
 */
public enum RelevanceBasis {
    LIMITED_DATA_COLD_START("Limited Data (Cold Start)"),
    PROFILE_BASED("Profile-Based Relevance"),
    FEEDBACK_SUPPORTED("Feedback-Supported Personalization");

    private final String displayLabel;

    RelevanceBasis(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
}
