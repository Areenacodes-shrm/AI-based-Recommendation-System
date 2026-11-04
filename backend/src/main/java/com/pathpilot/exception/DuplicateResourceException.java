package com.pathpilot.exception;

/**
 * Thrown when attempting to insert an entity violating uniqueness constraints (e.g., duplicate student email).
 */
public class DuplicateResourceException extends PathPilotException {
    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(String.format("%s with %s '%s' already exists.", resourceName, field, value));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
