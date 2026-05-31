package com.cinereserve.ui.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * UI / Selenium configuration.
 *
 * <p>Values are resolved in this priority order:
 * <ol>
 *   <li>JVM system property  (e.g. {@code -Dui.browser=firefox})</li>
 *   <li>{@code test.properties} on the classpath</li>
 *   <li>Hard-coded default</li>
 * </ol>
 *
 * <p>Add new properties to {@code src/test/resources/test.properties} to
 * configure the UI layer without touching source code.</p>
 */
public final class UIConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = UIConfig.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ignored) {
            // fall through to hard-coded defaults
        }
    }

    private UIConfig() {}

    // ─── Browser ──────────────────────────────────────────────────────────────

    /** Browser to launch: {@code chrome} (default), {@code firefox}, or {@code edge}. */
    public static final String BROWSER =
            get("ui.browser", "chrome");

    /** Run browser in headless mode — set {@code true} in CI environments. */
    public static final boolean HEADLESS =
            Boolean.parseBoolean(get("ui.headless", "false"));

    // ─── URLs ─────────────────────────────────────────────────────────────────

    /** Frontend application base URL (no trailing slash). */
    public static final String BASE_URL =
            get("ui.base.url", "http://localhost:3000");

    // ─── Timeouts ─────────────────────────────────────────────────────────────

    /** Selenium implicit wait in seconds. */
    public static final int IMPLICIT_WAIT_SECONDS =
            Integer.parseInt(get("ui.implicit.wait.seconds", "5"));

    /** Selenium explicit wait in seconds (for {@link org.openqa.selenium.support.ui.WebDriverWait}). */
    public static final int EXPLICIT_WAIT_SECONDS =
            Integer.parseInt(get("ui.explicit.wait.seconds", "15"));

    /** Page-load timeout in seconds. */
    public static final int PAGE_LOAD_TIMEOUT_SECONDS =
            Integer.parseInt(get("ui.page.load.timeout.seconds", "30"));

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Resolves a value from system property → properties file → defaultValue. */
    private static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPS.getProperty(key, defaultValue));
    }
}

