package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for /booking/:showtimeId route (seat selection).
 */
public class BookingCreationPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By seatGrid       = By.cssSelector("[class*='seat'], [data-testid*='seat'], [class*='grid']");
    private final By pageContent    = By.cssSelector("main, [class*='booking'], [class*='seat-selection']");
    private final By bookingHeading = By.xpath("//*[contains(text(),'Book') or contains(text(),'Select') or contains(text(),'Seat')]");

    public BookingCreationPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    public BookingCreationPage open(long showtimeId) {
        driver.get(UIConfig.BASE_URL + "/booking/" + showtimeId);
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
            return url.contains("/booking/") && !url.contains("/login");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSeatGridVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(seatGrid)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
