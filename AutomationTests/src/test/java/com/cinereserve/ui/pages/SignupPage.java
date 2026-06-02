package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for /signup route.
 */
public class SignupPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By nameInput     = By.cssSelector("input[name='name'], input[placeholder*='name'], input[placeholder*='Name']");
    private final By emailInput    = By.cssSelector("input[type='email']");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By submitButton  = By.cssSelector("button[type='submit']");
    private final By loginLink     = By.cssSelector("a[href*='login']");
    private final By pageHeading   = By.cssSelector("h1");

    public SignupPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public SignupPage open() {
        driver.get(UIConfig.BASE_URL + "/signup");
        waitForPageLoad();
        return this;
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    public SignupPage enterName(String name) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
            el.clear();
            el.sendKeys(name);
        } catch (Exception ignored) {}
        return this;
    }

    public SignupPage enterEmail(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear();
        el.sendKeys(email);
        return this;
    }

    public SignupPage enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
        return this;
    }

    public void clickSignup() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public void signupWith(String name, String email, String password) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        clickSignup();
    }

    public void clickLoginLink() {
        wait.until(ExpectedConditions.elementToBeClickable(loginLink)).click();
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public String getHeadingText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isEmailFieldVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordFieldVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginLinkVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginLink)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isRedirectedAwayFromSignup() {
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/signup")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

