package com.pathpilot.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.init.ScriptException;

import java.sql.SQLException;

/**
 * Translates startup JDBC connectivity and SQL script failures into clear,
 * non-secret troubleshooting instructions without exposing credentials.
 */
public class DatabaseConnectionFailureAnalyzer extends AbstractFailureAnalyzer<Throwable> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
        Throwable cursor = rootFailure;
        while (cursor != null) {
            if (cursor instanceof CannotGetJdbcConnectionException
                    || cursor instanceof SQLException
                    || cursor instanceof ScriptException) {
                String summary = sanitize(cursor.getMessage());
                String description = "PathPilot AI failed to initialize or connect to the configured relational database.\n"
                        + "Reason: " + summary;
                String action = "Verify your database settings and environment variables:\n"
                        + "  1. If using MySQL 8, confirm the MySQL server is running and the schema/port in DB_URL are reachable.\n"
                        + "  2. Confirm DB_URL, DB_USERNAME, DB_PASSWORD, and DB_DRIVER are set accurately (see .env.example).\n"
                        + "  3. To run immediately with the built-in H2 (MySQL-mode) database, unset DB_URL/DB_DRIVER and restart.";
                return new FailureAnalysis(description, action, cursor);
            }
            cursor = cursor.getCause();
        }
        return null;
    }

    private String sanitize(String msg) {
        if (msg == null || msg.isBlank()) {
            return "Unable to establish JDBC connection.";
        }
        // Strip inline URL credentials or overly verbose driver parameter lists
        return msg.replaceAll("password=[^&;\\s]+", "password=***");
    }
}
