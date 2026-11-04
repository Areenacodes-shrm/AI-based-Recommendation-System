package com.pathpilot.model;

public enum FeedbackAction {
    SAVED,
    DISMISSED,
    RATED,
    COMPLETED;

    public static FeedbackAction fromString(String val) {
        if (val == null) return SAVED;
        try {
            return FeedbackAction.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SAVED;
        }
    }
}
