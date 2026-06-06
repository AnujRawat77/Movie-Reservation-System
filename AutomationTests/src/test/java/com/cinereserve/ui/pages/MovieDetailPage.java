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
 * Page Object for /movies/:id route.
 */
public class MovieDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By movieTitle    = By.cssSelector("h1, h2[class*='title'], [class*='movie-title']");
    private final By movieRating   = By.cssSelector("[class*='rating'], [aria-label*='rating']");
    private final By bookButton    = By.cssSelector("button[class*='book'], a[href*='booking'], button:contains('Book')");
    private final By showtimesList = By.cssSelector("[class*='showtime'], [data-testid*='showtime']");
    private final By backButton    = By.cssSelector("button[class*='back'], a[href*='movies']");
    private final By posterImage   = By.cssSelector("img[alt*='poster'], img[src*='poster']");

    public MovieDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public MovieDetailPage open(int movieId) {
        driver.get(UIConfig.BASE_URL + "/movies/" + movieId);
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

    public String getMovieTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(movieTitle)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isMovieTitleVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(movieTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBookButtonVisible() {
        try {
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            return buttons.stream().anyMatch(b -> {
                String text = b.getText().toLowerCase();
                return text.contains("book") || text.contains("seat") || text.contains("ticket");
            });
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasShowtimes() {
        try {
            List<WebElement> elements = driver.findElements(showtimesList);
            if (!elements.isEmpty()) return true;
            // Also check for any time-related content
            return driver.findElements(By.xpath("//*[contains(text(),'showtime') or contains(text(),'Showtime') or contains(text(),'Schedule') or contains(@class,'showtime')]")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean isPageLoaded() {
        try {
            waitForPageLoad();
            return driver.findElements(movieTitle).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

