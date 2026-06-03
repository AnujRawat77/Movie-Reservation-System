package com.cinereserve.ui.steps;

import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.BookingsPage;
import com.cinereserve.ui.pages.LoginPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the My Bookings list and Booking Detail page scenarios.
 */
public class BookingsSteps {

    private static final Logger log = LogManager.getLogger(BookingsSteps.class);

    private final DriverContext ctx;
    private BookingsPage bookingsPage;

    public BookingsSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("the user is logged in as a valid user")
    public void loginAsValidUser() {
        LoginPage loginPage = new LoginPage(ctx.driver());
        loginPage.open();
        loginPage.enterEmail("user@cinereserve.com");
        loginPage.enterPassword("User@123");
        loginPage.clickLogin();
        try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Allure.step("Logged in as user@cinereserve.com");
        log.info("Logged in as valid user");
    }

    @Given("the user navigates to the My Bookings page")
    public void navigateToBookingsPage() {
        bookingsPage = new BookingsPage(ctx.driver());
        bookingsPage.open();
        Allure.step("Navigated to My Bookings: " + UIConfig.BASE_URL + "/bookings");
        log.info("Navigated to My Bookings page");
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the user should be on the My Bookings page")
    public void assertOnBookingsPage() {
        assertThat(bookingsPage.isOnBookingsPage())
                .as("Expected to be on My Bookings page, but URL is: " + bookingsPage.getCurrentUrl())
                .isTrue();
        Allure.step("Confirmed on My Bookings page");
        log.info("Verified My Bookings page loaded");
    }

    @Then("the bookings page should load without errors")
    public void assertBookingsPageLoads() {
        // Page loaded if we're on /bookings (even with 0 bookings)
        assertThat(bookingsPage.getCurrentUrl())
                .as("Expected /bookings in URL")
                .contains("/bookings");
        Allure.step("Bookings page loaded without errors");
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the user clicks View Details on the first booking")
    public void clickViewDetailsOnFirstBooking() {
        if (bookingsPage == null) bookingsPage = new BookingsPage(ctx.driver());
        bookingsPage.clickViewDetailsFirst();
        Allure.step("Clicked View Details on first booking");
        log.info("Clicked View Details link");
    }

    @When("the user clicks the back link on the detail page")
    public void clickBackLink() {
        if (bookingsPage == null) bookingsPage = new BookingsPage(ctx.driver());
        bookingsPage.clickBackLink();
        Allure.step("Clicked Back link on detail page");
        log.info("Clicked Back link");
    }

    // ── Then (detail page) ────────────────────────────────────────────────────

    @Then("the user should be on the booking detail page")
    public void assertOnDetailPage() {
        assertThat(bookingsPage.isOnDetailPage())
                .as("Expected URL to match /bookings/{id}, but got: " + bookingsPage.getCurrentUrl())
                .isTrue();
        Allure.step("Confirmed on Booking Detail page");
        log.info("Verified Booking Detail page URL");
    }

    @Then("the detail page should display the movie title")
    public void assertDetailShowsMovieTitle() {
        assertThat(bookingsPage.detailPageShowsMovieTitle())
                .as("Expected movie title to be visible on detail page")
                .isTrue();
        Allure.step("Movie title visible on detail page");
    }

    @Then("the detail page should display the booking status")
    public void assertDetailShowsStatus() {
        assertThat(bookingsPage.detailPageShowsStatus())
                .as("Expected booking status badge to be visible")
                .isTrue();
        Allure.step("Booking status badge visible");
    }

    @Then("the detail page should display the total amount")
    public void assertDetailShowsTotalAmount() {
        assertThat(bookingsPage.detailPageShowsTotalAmount())
                .as("Expected total amount with $ sign to be visible")
                .isTrue();
        Allure.step("Total amount visible on detail page");
    }

    @Then("the detail page should display the booked seats")
    public void assertDetailShowsSeats() {
        assertThat(bookingsPage.detailPageShowsSeats())
                .as("Expected at least one seat badge to be visible")
                .isTrue();
        Allure.step("Seat badges visible on detail page");
    }

    @Then("the Download Receipt button should be visible on the detail page")
    public void assertDownloadButtonVisible() {
        assertThat(bookingsPage.downloadReceiptButtonVisible())
                .as("Expected Download Receipt button to be visible")
                .isTrue();
        Allure.step("Download Receipt button visible");
    }

    @Then("the user should be back on the My Bookings page")
    public void assertBackOnBookingsPage() {
        assertThat(bookingsPage.getCurrentUrl())
                .as("Expected to return to My Bookings (/bookings)")
                .contains("/bookings");
        Allure.step("Returned to My Bookings page");
        log.info("Verified return to My Bookings page");
    }
}
