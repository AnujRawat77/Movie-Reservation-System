package com.cinereserve.ui.context;

import com.cinereserve.ui.config.UIConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Scenario-scoped WebDriver holder for Cucumber BDD tests.
 *
 * <p>PicoContainer instantiates one {@code DriverContext} per scenario and
 * injects it into every step-definition class and {@code Hooks} that
 * declares it as a constructor parameter — ensuring all classes in the
 * same scenario share the <em>same</em> driver instance.</p>
 *
 * <pre>
 * // Step definition example
 * public class LoginSteps {
 *
 *     private final DriverContext ctx;
 *
 *     public LoginSteps(DriverContext ctx) {   // PicoContainer injects this
 *         this.ctx = ctx;
 *     }
 *
 *     &#64;Given("the user opens the login page")
 *     public void openLoginPage() {
 *         ctx.driver().get(UIConfig.BASE_URL + "/login");
 *     }
 * }
 * </pre>
 *
 * @see com.cinereserve.ui.hooks.Hooks  lifecycle management (Before / After)
 */
public class DriverContext {

    private WebDriver driver;
    private WebDriverWait wait;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /** Initialises the WebDriver using settings from {@link UIConfig}. */
    public void init() {
        driver = createDriver(UIConfig.BROWSER, UIConfig.HEADLESS);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(UIConfig.IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(UIConfig.PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    /** Quits the WebDriver session. Called from {@link com.cinereserve.ui.hooks.Hooks#tearDown}. */
    public void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait   = null;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the active {@link WebDriver} for the current scenario. */
    public WebDriver driver() {
        return driver;
    }

    /** Returns a pre-configured {@link WebDriverWait} for the current scenario. */
    public WebDriverWait getWait() {
        return wait;
    }

    // ─── Navigation helpers ───────────────────────────────────────────────────

    /**
     * Navigates to a path relative to {@link UIConfig#BASE_URL}.
     *
     * @param path e.g. {@code "/login"}
     */
    public void navigateTo(String path) {
        driver.get(UIConfig.BASE_URL + path);
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    private WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield new FirefoxDriver(opts);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                org.openqa.selenium.edge.EdgeOptions opts = new org.openqa.selenium.edge.EdgeOptions();
                if (headless) opts.addArguments("--headless=new");
                yield new org.openqa.selenium.edge.EdgeDriver(opts);
            }
            default -> {  // chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(opts);
            }
        };
    }
}

