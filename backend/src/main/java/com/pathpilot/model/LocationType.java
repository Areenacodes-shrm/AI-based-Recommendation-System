package com.pathpilot.model;

public enum LocationType {
    REMOTE,
    HYBRID,
    ON_SITE;

    public static LocationType fromString(String val) {
        if (val == null) return REMOTE;
        try {
            return LocationType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return REMOTE;
        }
    }
}
