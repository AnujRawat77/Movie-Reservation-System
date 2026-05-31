package com.cinereserve.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Shared configuration resolver used by both API and UI test layers.
 *
 * <p>Values are resolved in the following priority order:
 * <ol>
 *   <li>JVM system property  (highest priority, useful for CI overrides)</li>
 *   <li>Environment variable (automatically mapped: dots → underscores, upper-cased)</li>
 *   <li>{@code test.properties} on the classpath</li>
 *   <li>Hard-coded {@code defaultValue}  (lowest priority)</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>
 * String apiUrl = EnvironmentUtil.get("api.base.url", "http://localhost:8080");
 * int timeout   = EnvironmentUtil.getInt("connect.timeout.ms", 10_000);
 * boolean flag  = EnvironmentUtil.getBoolean("ui.headless", false);
 * </pre>
 */
public final class EnvironmentUtil {

    private static final Logger log = LogManager.getLogger(EnvironmentUtil.class);

    private static final Properties FILE_PROPS = new Properties();

    static {
        try (InputStream in = EnvironmentUtil.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (in != null) {
                FILE_PROPS.load(in);
                log.debug("Loaded test.properties ({} entries)", FILE_PROPS.size());
            } else {
                log.warn("test.properties not found on classpath — using defaults only");
            }
        } catch (IOException e) {
            log.warn("Could not load test.properties: {}", e.getMessage());
        }
    }

    private EnvironmentUtil() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Resolves a string configuration value.
     *
     * @param key          property key (e.g. {@code "api.base.url"})
     * @param defaultValue fallback when the key is not defined anywhere
     * @return resolved value — never {@code null}
     */
    public static String get(String key, String defaultValue) {
        // 1. System property
        String value = System.getProperty(key);
        if (value != null) return value;

        // 2. Environment variable (dots → underscores, upper-cased)
        value = System.getenv(toEnvVarName(key));
        if (value != null) return value;

        // 3. test.properties
        value = FILE_PROPS.getProperty(key);
        if (value != null) return value;

        // 4. Default
        return defaultValue;
    }

    /**
     * Resolves an integer configuration value.
     *
     * @param key          property key
     * @param defaultValue fallback value
     * @return resolved integer
     */
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Resolves a boolean configuration value.
     *
     * @param key          property key
     * @param defaultValue fallback value
     * @return resolved boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    /**
     * Resolves a long configuration value.
     *
     * @param key          property key
     * @param defaultValue fallback value
     * @return resolved long
     */
    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.warn("Invalid long for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Converts a property key to its environment-variable equivalent.
     * e.g. {@code "api.base.url"} → {@code "API_BASE_URL"}
     */
    private static String toEnvVarName(String key) {
        return key.replace('.', '_').replace('-', '_').toUpperCase();
    }
}

