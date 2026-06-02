package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for admin routes (/admin/*).
 */
public class AdminPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By dashboardHeading = By.cssSelector("h1, h2");
    private final By moviesNavLink    = By.cssSelector("a[href*='admin/movies'], nav a:nth-child(2)");
    private final By usersNavLink     = By.cssSelector("a[href*='admin/users']");
    private final By reportsNavLink   = By.cssSelector("a[href*='admin/reports']");
    private final By hallsNavLink     = By.cssSelector("a[href*='admin/halls']");
    private final By showtimesNavLink = By.cssSelector("a[href*='admin/showtimes']");
    private final By tableRows        = By.cssSelector("table tbody tr, [role='row']");
    private final By addButton        = By.cssSelector("button:contains('Add'), button[class*='add'], button[class*='create']");

    public AdminPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public AdminPage openDashboard() {
        driver.get(UIConfig.BASE_URL + "/admin");
        waitForPageLoad();
        return this;
    }

    public AdminPage openMovies() {
        driver.get(UIConfig.BASE_URL + "/admin/movies");
        waitForPageLoad();
        return this;
    }

    public AdminPage openUsers() {
        driver.get(UIConfig.BASE_URL + "/admin/users");
        waitForPageLoad();
        return this;
    }

    public AdminPage openReports() {
        driver.get(UIConfig.BASE_URL + "/admin/reports");
        waitForPageLoad();
        return this;
    }

    public AdminPage openHalls() {
        driver.get(UIConfig.BASE_URL + "/admin/halls");
        waitForPageLoad();
        return this;
    }

    public AdminPage openShowtimes() {
        driver.get(UIConfig.BASE_URL + "/admin/showtimes");
        waitForPageLoad();
        return this;
    }

    public void waitForPageLoad() {
        wait.until(d -> {
            String readyState = (String) ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getHeadingText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardHeading)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isDashboardLoaded() {
        try {
            waitForPageLoad();
            String url = driver.getCurrentUrl();
            return url.contains("/admin") && !url.contains("/login");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRedirectedToLogin() {
        try {
            wait.until(ExpectedConditions.urlContains("/login"));
            return true;
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("/login");
        }
    }

    public int getTableRowCount() {
        try {
            List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }
}

