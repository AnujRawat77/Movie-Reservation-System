package com.cinereserve.ui.base;

import com.cinereserve.ui.config.UIConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Base class for all Selenium UI tests (TestNG style).
 *
 * <p>Extend this class to write page-based UI tests.
 * WebDriver is created fresh for each test method and torn down afterwards.
 * The browser and headless mode are controlled via {@link UIConfig} /
 * {@code test.properties} or system properties.</p>
 *
 * <pre>
 * class HomePageTest extends BaseUITest {
 *
 *     &#64;Test
 *     public void titleShouldContainCineReserve() {
 *         navigateTo("/");
 *         assertThat(driver.getTitle()).contains("CineReserve");
 *     }
 * }
 * </pre>
 *
 * @see UIConfig
 * @see com.cinereserve.ui.pages  Page Object Model classes live here
 */
public abstract class BaseUITest {

    protected static final Logger log = LogManager.getLogger(BaseUITest.class);

    /** The active WebDriver instance — available to all subclasses. */
    protected WebDriver driver;

    /** Pre-configured explicit wait — use for dynamic elements. */
    protected WebDriverWait wait;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUpDriver() {
        log.info("Initialising WebDriver — browser: {}, headless: {}",
                UIConfig.BROWSER, UIConfig.HEADLESS);

        driver = createDriver(UIConfig.BROWSER, UIConfig.HEADLESS);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(UIConfig.IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(UIConfig.PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
        log.info("WebDriver ready — {}", driver.getClass().getSimpleName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        if (driver != null) {
            log.info("Quitting WebDriver — {}", driver.getClass().getSimpleName());
            driver.quit();
            driver = null;
        }
    }

    // ─── Navigation helpers ───────────────────────────────────────────────────

    /**
     * Navigates to a path relative to {@link UIConfig#BASE_URL}.
     *
     * @param path e.g. {@code "/login"} or {@code "/movies"}
     */
    protected void navigateTo(String path) {
        String url = UIConfig.BASE_URL + path;
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    /**
     * Navigates to a fully-qualified URL.
     *
     * @param url absolute URL
     */
    protected void navigateToAbsolute(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    // ─── Driver factory ───────────────────────────────────────────────────────

    private WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("--headless");
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                org.openqa.selenium.edge.EdgeOptions options = new org.openqa.selenium.edge.EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                yield new org.openqa.selenium.edge.EdgeDriver(options);
            }
            default -> {  // chrome (default)
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(options);
            }
        };
    }
}

