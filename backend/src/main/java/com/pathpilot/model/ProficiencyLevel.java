package com.pathpilot.model;

/**
 * Standard proficiency level representing student skill mastery.
 */
public enum ProficiencyLevel {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3),
    EXPERT(4);

    private final int numericRank;

    ProficiencyLevel(int numericRank) {
        this.numericRank = numericRank;
    }

    public int getNumericRank() {
        return numericRank;
    }

    public static ProficiencyLevel fromString(String val) {
        if (val == null) return BEGINNER;
        try {
            return ProficiencyLevel.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BEGINNER;
        }
    }
}
