package com.cinereserve.ui.steps;

import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.BookingCreationPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Booking Creation flow (seat selection page).
 */
public class BookingCreationSteps {

    private static final Logger log = LogManager.getLogger(BookingCreationSteps.class);

    private final DriverContext ctx;
    private BookingCreationPage bookingCreationPage;

    public BookingCreationSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @Given("a showtime exists with id {int}")
    public void setShowtimeId(int showtimeId) {
        Allure.step("Using showtime id: " + showtimeId);
        log.info("Target showtime id set to {}", showtimeId);
    }

    @When("the user navigates to the booking page for showtime {int}")
    public void navigateToBookingPage(int showtimeId) {
        bookingCreationPage = new BookingCreationPage(ctx.driver());
        bookingCreationPage.open(showtimeId);
        Allure.step("Navigated to /booking/" + showtimeId);
        log.info("Navigated to booking page for showtime id={}", showtimeId);
    }

    @Then("the booking page should be visible")
    public void verifyBookingPageVisible() {
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Current URL: " + url);
        assertThat(url).doesNotContain("/login");
    }

    @Then("the seat grid or seat map should be visible")
    public void verifySeatGridVisible() {
        boolean visible = bookingCreationPage.isSeatGridVisible();
        Allure.step("Seat grid visible: " + visible);
        assertThat(visible).as("Seat grid or map should be displayed").isTrue();
    }
}
