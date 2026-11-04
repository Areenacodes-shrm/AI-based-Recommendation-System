package com.pathpilot.util;

import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

/**
 * Utility helper for robust JDBC key extraction across MySQL and H2 database engines.
 */
public final class JdbcUtils {

    private JdbcUtils() {}

    public static Long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && !keys.isEmpty()) {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                    return num.longValue();
                }
            }
            // If no key named 'id' exists, pick the first numeric value
            for (Object val : keys.values()) {
                if (val instanceof Number num) {
                    return num.longValue();
                }
            }
        }

        try {
            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
