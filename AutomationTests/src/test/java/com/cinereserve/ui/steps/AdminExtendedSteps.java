package com.cinereserve.ui.steps;

import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.AdminPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for admin extended pages: genres, halls, showtimes, reservations.
 */
public class AdminExtendedSteps {

    private static final Logger log = LogManager.getLogger(AdminExtendedSteps.class);

    private final DriverContext ctx;
    private AdminPage adminPage;

    public AdminExtendedSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @When("the user navigates to the admin genres page")
    public void navigateToAdminGenres() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openGenres();
        Allure.step("Navigated to admin genres page");
        log.info("Navigated to /admin/genres");
    }

    @When("the user navigates to the admin halls page")
    public void navigateToAdminHalls() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openHalls();
        Allure.step("Navigated to admin halls page");
        log.info("Navigated to /admin/halls");
    }

    @When("the user navigates to the admin showtimes page")
    public void navigateToAdminShowtimes() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openShowtimes();
        Allure.step("Navigated to admin showtimes page");
        log.info("Navigated to /admin/showtimes");
    }

    @When("the user navigates to the admin reservations page")
    public void navigateToAdminReservations() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openReservations();
        Allure.step("Navigated to admin reservations page");
        log.info("Navigated to /admin/reservations");
    }
}
