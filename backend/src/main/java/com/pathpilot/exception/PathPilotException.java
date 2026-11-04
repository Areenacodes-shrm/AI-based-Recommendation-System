package com.pathpilot.exception;

/**
 * Base unchecked exception for all PathPilot domain failures.
 */
public abstract class PathPilotException extends RuntimeException {
    public PathPilotException(String message) {
        super(message);
    }

    public PathPilotException(String message, Throwable cause) {
        super(message, cause);
    }
}
