package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the /profile route.
 */
public class ProfilePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageHeading      = By.xpath("//*[contains(text(),'Profile') or contains(text(),'profile')]");
    private final By userName         = By.cssSelector("h1, h2, [class*='name']");
    private final By loyaltySection   = By.xpath("//*[contains(text(),'Loyalty') or contains(text(),'loyalty') or contains(text(),'Points') or contains(text(),'points')]");
    private final By watchlistSection = By.xpath("//*[contains(text(),'Watchlist') or contains(text(),'watchlist')]");

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    public ProfilePage open() {
        driver.get(UIConfig.BASE_URL + "/profile");
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

    public boolean isPageVisible() {
        try {
            waitForPageLoad();
            String url = driver.getCurrentUrl();
            return url.contains("/profile");
        } catch (Exception e) {
            return false;
        }
    }

    public String getDisplayedUserName() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(userName)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isLoyaltySectionVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loyaltySection)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
