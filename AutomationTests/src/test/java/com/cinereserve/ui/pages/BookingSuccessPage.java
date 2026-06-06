package com.cinereserve.ui.pages;

import com.cinereserve.ui.config.UIConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for /booking/success/:id route.
 */
public class BookingSuccessPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By confirmationContent = By.xpath("//*[contains(text(),'Booking') or contains(text(),'Confirmed') or contains(text(),'Success') or contains(text(),'Reservation')]");
    private final By viewBookingsLink    = By.xpath("//a[contains(@href,'bookings') or contains(text(),'Bookings') or contains(text(),'bookings')]");

    public BookingSuccessPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    public BookingSuccessPage open(long reservationId) {
        driver.get(UIConfig.BASE_URL + "/booking/success/" + reservationId);
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
            return driver.getCurrentUrl().contains("/booking/success");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConfirmationContentVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationContent)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isViewBookingsLinkVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(viewBookingsLink)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
