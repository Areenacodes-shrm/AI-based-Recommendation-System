package com.pathpilot.model;

public enum ProjectTier {
    GUIDED,
    CAPSTONE,
    OPEN_SOURCE;

    public static ProjectTier fromString(String val) {
        if (val == null) return GUIDED;
        try {
            return ProjectTier.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GUIDED;
        }
    }
}
