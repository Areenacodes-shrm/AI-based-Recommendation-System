package com.pathpilot.model;

public enum PlanStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED;

    public static PlanStatus fromString(String val) {
        if (val == null) return PENDING;
        try {
            return PlanStatus.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
