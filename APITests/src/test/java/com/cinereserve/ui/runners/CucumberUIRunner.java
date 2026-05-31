package com.cinereserve.ui.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber TestNG runner for UI / BDD tests.
 *
 * <p>This class bridges Cucumber and TestNG so that the standard
 * {@code mvn test -P bdd-tests} command (or the {@code testng-suites/bdd.xml} suite)
 * discovers and executes all {@code .feature} files under
 * {@code src/test/resources/features/ui/}.</p>
 *
 * <h2>Key options</h2>
 * <ul>
 *   <li>{@code features}    — path to Cucumber feature files</li>
 *   <li>{@code glue}        — packages containing step definitions and hooks</li>
 *   <li>{@code plugin}      — reporting plugins (Allure, HTML, JSON, pretty console)</li>
 *   <li>{@code tags}        — filter scenarios at runtime, e.g.
 *       {@code -Dcucumber.filter.tags="@smoke"}</li>
 *   <li>{@code monochrome}  — cleaner console output</li>
 * </ul>
 *
 * <h2>Running from Maven</h2>
 * <pre>
 *   # All BDD UI scenarios
 *   mvn test -P bdd-tests
 *
 *   # Only @smoke tagged scenarios
 *   mvn test -P bdd-tests -Dcucumber.filter.tags="@smoke"
 * </pre>
 */
@CucumberOptions(
        // Feature file location
        features = "src/test/resources/features/ui",

        // Step definitions + Hooks packages (add more packages as needed)
        glue = {
                "com.cinereserve.ui.hooks",
                "com.cinereserve.ui.steps"
        },

        // Reporting plugins
        plugin = {
                // Allure Cucumber 7 integration — rich HTML report
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                // Pretty console output
                "pretty",
                // HTML report (target/cucumber-reports/ui.html)
                "html:target/cucumber-reports/ui.html",
                // JSON report for CI integrations
                "json:target/cucumber-reports/ui.json"
        },

        // Show full step descriptions in console
        monochrome = true,

        // Publish Cucumber reports to https://reports.cucumber.io (optional)
        publish = false
)
public class CucumberUIRunner extends AbstractTestNGCucumberTests {

    /**
     * Runs each scenario as a separate TestNG data-provider entry.
     * Set {@code parallel = true} to run scenarios in parallel
     * (requires thread-safe Page Objects and a separate WebDriver per thread).
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}

