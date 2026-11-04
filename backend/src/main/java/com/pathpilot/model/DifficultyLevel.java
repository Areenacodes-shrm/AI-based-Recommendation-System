package com.pathpilot.model;

/**
 * Standard difficulty level for learning opportunities.
 */
public enum DifficultyLevel {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3),
    EXPERT(4);

    private final int numericRank;

    DifficultyLevel(int numericRank) {
        this.numericRank = numericRank;
    }

    public int getNumericRank() {
        return numericRank;
    }

    public static DifficultyLevel fromString(String val) {
        if (val == null) return INTERMEDIATE;
        try {
            return DifficultyLevel.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return INTERMEDIATE;
        }
    }
}
