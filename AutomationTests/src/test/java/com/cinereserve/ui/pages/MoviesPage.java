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
 * Page Object for / (home/movies list) route.
 */
public class MoviesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By movieCards    = By.cssSelector("[data-testid='movie-card'], .movie-card, article, .grid > a");
    private final By navLoginLink  = By.cssSelector("a[href*='login']");
    private final By navSignupLink = By.cssSelector("a[href*='signup']");
    private final By pageHeading   = By.cssSelector("h1, h2");
    private final By heroSection   = By.cssSelector("main, section, .hero");
    private final By movieTitle    = By.cssSelector("h2, h3, [class*='title']");
    private final By navBar        = By.cssSelector("nav, header");

    public MoviesPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public MoviesPage open() {
        driver.get(UIConfig.BASE_URL + "/");
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

    // ── Actions ──────────────────────────────────────────────────────────────

    public void clickLoginLink() {
        wait.until(ExpectedConditions.elementToBeClickable(navLoginLink)).click();
    }

    public void clickSignupLink() {
        wait.until(ExpectedConditions.elementToBeClickable(navSignupLink)).click();
    }

    public MovieDetailPage clickFirstMovie() {
        List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(movieCards));
        if (!cards.isEmpty()) {
            cards.get(0).click();
        }
        return new MovieDetailPage(driver);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isNavBarVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(navBar)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginLinkVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(navLoginLink)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignupLinkVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(navSignupLink)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getMovieCardCount() {
        try {
            List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(movieCards));
            return cards.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasMoviesDisplayed() {
        return getMovieCardCount() > 0;
    }

    public boolean isPageLoaded() {
        try {
            waitForPageLoad();
            return driver.findElements(heroSection).size() > 0
                    || driver.findElements(movieTitle).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

