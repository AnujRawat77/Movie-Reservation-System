package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for /login route.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By emailInput    = By.cssSelector("input[type='email']");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By submitButton  = By.cssSelector("button[type='submit']");
    private final By signupLink    = By.cssSelector("a[href*='signup']");
    private final By errorToast    = By.cssSelector("[data-sonner-toast]");
    private final By pageHeading   = By.cssSelector("h1");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public LoginPage open() {
        driver.get(UIConfig.BASE_URL + "/login");
        waitForPageLoad();
        return this;
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    public LoginPage enterEmail(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear();
        el.sendKeys(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
        return this;
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public void loginWith(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    public void clickSignupLink() {
        wait.until(ExpectedConditions.elementToBeClickable(signupLink)).click();
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public String getHeadingText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading)).getText();
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

    public boolean isSubmitButtonEnabled() {
        try {
            WebElement btn = driver.findElement(submitButton);
            return btn.isDisplayed() && btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignupLinkVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(signupLink)).isDisplayed();
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

    public boolean isRedirectedAwayFromLogin() {
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasToastMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorToast));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

