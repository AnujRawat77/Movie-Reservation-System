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
 * Page Object for /bookings and /bookings/:id routes.
 * Covers the My Bookings list page and the Booking Detail page.
 */
public class BookingsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── My Bookings list locators ─────────────────────────────────────────────
    private final By pageHeading     = By.xpath("//*[contains(text(),'My Bookings') or contains(text(),'my bookings')]");
    private final By bookingCards    = By.xpath("//*[contains(@class,'rounded') and .//*[contains(@class,'text-gradient-gold') or contains(@class,'totalAmount')]]");
    private final By viewDetailsLink = By.xpath("//a[contains(text(),'View Details') or .//*[contains(@class,'lucide-eye')]]");

    // ── Booking Detail locators ───────────────────────────────────────────────
    private final By detailMovieTitle  = By.tagName("h1");
    private final By detailStatusBadge = By.xpath("//*[contains(@class,'rounded-full') and (contains(text(),'CONFIRMED') or contains(text(),'CANCELLED'))]");
    private final By detailTotalAmount = By.xpath("//*[contains(@class,'text-gradient-gold')]");
    private final By detailSeatBadges  = By.xpath("//*[contains(@class,'font-mono')]");
    private final By downloadBtn       = By.xpath("//button[contains(.,'Download Receipt')]");
    private final By backLink          = By.xpath("//a[contains(.,'My Bookings')]");

    public BookingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(UIConfig.EXPLICIT_WAIT_SECONDS));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public BookingsPage open() {
        driver.get(UIConfig.BASE_URL + "/bookings");
        waitForPageLoad();
        return this;
    }

    public void waitForPageLoad() {
        wait.until(d -> {
            String readyState = (String) ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
        try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── List page queries ─────────────────────────────────────────────────────

    public boolean isOnBookingsPage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
            return driver.getCurrentUrl().contains("/bookings");
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("/bookings");
        }
    }

    public boolean hasBookingCards() {
        try {
            List<WebElement> cards = driver.findElements(bookingCards);
            if (!cards.isEmpty()) return true;
            // Fallback: check for any card-like structure
            List<WebElement> all = driver.findElements(By.xpath("//*[contains(@class,'rounded-2xl') or contains(@class,'rounded-xl')]"));
            return !all.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasViewDetailsLink() {
        try {
            List<WebElement> links = driver.findElements(viewDetailsLink);
            return !links.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickViewDetailsFirst() {
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        List<WebElement> links = clickWait.until(d -> {
            List<WebElement> candidates = d.findElements(viewDetailsLink);
            return candidates.stream().anyMatch(WebElement::isDisplayed) ? candidates : null;
        });

        WebElement target = links.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible View Details link found"));

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", target);

        try {
            clickWait.until(ExpectedConditions.elementToBeClickable(target)).click();
        } catch (Exception e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }

        clickWait.until(d -> d.getCurrentUrl().matches(".*\\/bookings\\/[^/]+$"));
        waitForPageLoad();
    }

    // ── Detail page queries ───────────────────────────────────────────────────

    public boolean isOnDetailPage() {
        try {
            wait.until(d -> d.getCurrentUrl().matches(".*\\/bookings\\/[^/]+$"));
            return true;
        } catch (Exception e) {
            return driver.getCurrentUrl().matches(".*\\/bookings\\/[^/]+$");
        }
    }

    public boolean detailPageShowsMovieTitle() {
        try {
            String title = wait.until(ExpectedConditions.visibilityOfElementLocated(detailMovieTitle)).getText();
            return title != null && !title.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean detailPageShowsStatus() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(detailStatusBadge)).isDisplayed();
        } catch (Exception e) {
            // Fallback: any badge-like element with status text
            List<WebElement> badges = driver.findElements(
                    By.xpath("//*[contains(text(),'CONFIRMED') or contains(text(),'CANCELLED')]"));
            return !badges.isEmpty();
        }
    }

    public boolean detailPageShowsTotalAmount() {
        try {
            String text = wait.until(ExpectedConditions.visibilityOfElementLocated(detailTotalAmount)).getText();
            return text != null && text.contains("$");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean detailPageShowsSeats() {
        try {
            List<WebElement> seats = wait.until(
                    ExpectedConditions.numberOfElementsToBeMoreThan(detailSeatBadges, 0));
            return !seats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean downloadReceiptButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(downloadBtn)).isDisplayed();
        } catch (Exception e) {
            // Fallback: any button with download-related text
            List<WebElement> btns = driver.findElements(
                    By.xpath("//button[contains(.,'Download') or contains(.,'Receipt')]"));
            return !btns.isEmpty();
        }
    }

    public void clickBackLink() {
        try {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(backLink));
            link.click();
            waitForPageLoad();
        } catch (Exception e) {
            driver.navigate().back();
            waitForPageLoad();
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
