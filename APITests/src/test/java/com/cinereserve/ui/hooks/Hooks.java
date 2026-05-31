package com.cinereserve.ui.hooks;

import com.cinereserve.ui.context.DriverContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

/**
 * Cucumber lifecycle hooks for UI / BDD tests.
 *
 * <p>PicoContainer injects the scenario-scoped {@link DriverContext} here
 * <em>and</em> into every step-definition class, guaranteeing all classes
 * within one scenario share the same WebDriver instance.</p>
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>{@code @Before} — initialise the WebDriver via {@link DriverContext#init()}</li>
 *   <li>{@code @After}  — capture a screenshot on failure, attach it to Allure
 *       and the Cucumber report, then quit the driver</li>
 * </ul>
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    private final DriverContext ctx;

    /** PicoContainer injects the scenario-scoped {@link DriverContext}. */
    public Hooks(DriverContext ctx) {
        this.ctx = ctx;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        log.info("▶ Scenario START — \"{}\" [{}]", scenario.getName(), scenario.getId());
        ctx.init();
        log.info("WebDriver initialised for scenario: {}", scenario.getName());
    }

    @After(order = 0)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && ctx.driver() != null) {
            log.warn("Scenario FAILED — capturing screenshot: \"{}\"", scenario.getName());
            try {
                byte[] screenshot = ((TakesScreenshot) ctx.driver())
                        .getScreenshotAs(OutputType.BYTES);

                // Attach to Allure report
                Allure.addAttachment(
                        "Screenshot on failure — " + scenario.getName(),
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        ".png");

                // Attach to Cucumber HTML report
                scenario.attach(screenshot, "image/png", "Screenshot on failure");

            } catch (Exception e) {
                log.error("Failed to capture screenshot: {}", e.getMessage());
            }
        }

        log.info("◀ Scenario END — \"{}\" [{}]", scenario.getName(), scenario.getStatus());
        ctx.quit();
    }
}

