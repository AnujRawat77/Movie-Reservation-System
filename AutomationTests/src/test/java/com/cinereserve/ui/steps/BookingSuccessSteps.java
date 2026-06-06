package com.cinereserve.ui.steps;

import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.BookingSuccessPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Booking Success / Confirmation page.
 */
public class BookingSuccessSteps {

    private static final Logger log = LogManager.getLogger(BookingSuccessSteps.class);

    private final DriverContext ctx;
    private BookingSuccessPage bookingSuccessPage;

    public BookingSuccessSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @When("the user navigates to the booking success page with id {int}")
    public void navigateToBookingSuccessPage(int reservationId) {
        bookingSuccessPage = new BookingSuccessPage(ctx.driver());
        bookingSuccessPage.open(reservationId);
        Allure.step("Navigated to /booking/success/" + reservationId);
        log.info("Navigated to booking success page, id={}", reservationId);
    }

    @Then("the booking success page should be visible")
    public void verifyBookingSuccessPageVisible() {
        boolean visible = bookingSuccessPage.isPageVisible();
        Allure.step("Booking success page visible: " + visible);
        assertThat(visible).as("Booking success page should load").isTrue();
    }

    @Then("the page should display confirmation content")
    public void verifyConfirmationContent() {
        boolean hasContent = bookingSuccessPage.isConfirmationContentVisible();
        Allure.step("Confirmation content visible: " + hasContent);
        assertThat(hasContent).as("Confirmation content should be visible").isTrue();
    }

    @Then("there should be a link or button to view bookings")
    public void verifyViewBookingsLink() {
        boolean hasLink = bookingSuccessPage.isViewBookingsLinkVisible();
        Allure.step("View bookings link visible: " + hasLink);
        assertThat(hasLink).as("View bookings link/button should be on success page").isTrue();
    }
}
