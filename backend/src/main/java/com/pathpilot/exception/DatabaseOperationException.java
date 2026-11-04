package com.pathpilot.exception;

/**
 * Wraps low-level JDBC and database errors into a clean, sanitized application exception.
 */
public class DatabaseOperationException extends PathPilotException {
    public DatabaseOperationException(String operationDescription, Throwable cause) {
        super("Database error during operation: " + operationDescription, cause);
    }

    public DatabaseOperationException(String message) {
        super(message);
    }
}
