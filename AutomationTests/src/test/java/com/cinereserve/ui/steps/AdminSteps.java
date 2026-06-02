package com.cinereserve.ui.steps;

import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.AdminPage;
import com.cinereserve.ui.pages.LoginPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Admin UI scenarios.
 */
public class AdminSteps {

    private static final Logger log = LogManager.getLogger(AdminSteps.class);

    private final DriverContext ctx;
    private AdminPage adminPage;

    public AdminSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    // ── Given ────────────────────────────────────────────────────────────────

    @Given("the user navigates to the admin dashboard without authentication")
    public void navigateToAdminWithoutAuth() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openDashboard();
        Allure.step("Navigated to admin dashboard without auth");
        log.info("Navigated to /admin without authentication");
    }

    @Given("the user is logged in as admin")
    public void loginAsAdmin() {
        // Login via UI
        ctx.navigateTo("/login");
        LoginPage loginPage = new LoginPage(ctx.driver());
        loginPage.waitForPageLoad();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
        String currentUrl = ctx.driver().getCurrentUrl();
        Allure.step("Logged in as admin, current URL: " + currentUrl);
        log.info("Admin login attempt completed, URL: {}", currentUrl);
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @When("the user navigates to the admin dashboard")
    public void navigateToAdminDashboard() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openDashboard();
        Allure.step("Navigated to admin dashboard");
    }

    @When("the user navigates to the admin movies page")
    public void navigateToAdminMovies() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openMovies();
        Allure.step("Navigated to admin movies page");
    }

    @When("the user navigates to the admin users page")
    public void navigateToAdminUsers() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openUsers();
        Allure.step("Navigated to admin users page");
    }

    @When("the user navigates to the admin reports page")
    public void navigateToAdminReports() {
        adminPage = new AdminPage(ctx.driver());
        adminPage.openReports();
        Allure.step("Navigated to admin reports page");
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Then("the user should not be able to access the admin area")
    public void verifyAdminNotAccessible() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("URL after admin access attempt: " + url);
        log.info("URL after unauthenticated admin access: {}", url);
        // Should redirect to login OR show 403/unauthorized
        boolean isRedirectedOrBlocked = url.contains("/login") || !url.contains("/admin");
        assertThat(isRedirectedOrBlocked)
                .as("Unauthenticated user should be redirected from admin area, but was at: " + url)
                .isTrue();
    }

    @Then("the admin dashboard should be accessible")
    public void verifyAdminDashboardAccessible() {
        if (adminPage == null) {
            adminPage = new AdminPage(ctx.driver());
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Admin URL: " + url);
        log.info("Admin dashboard URL: {}", url);
        boolean accessible = adminPage.isDashboardLoaded();
        assertThat(accessible)
                .as("Admin dashboard should be accessible after login, URL: " + url)
                .isTrue();
    }

    @Then("the admin page should load without errors")
    public void verifyAdminPageLoadsWithoutErrors() {
        if (adminPage == null) adminPage = new AdminPage(ctx.driver());
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Admin page URL: " + url);
        boolean loaded = !url.contains("error") && !url.contains("404");
        assertThat(loaded)
                .as("Admin page should load without errors, URL: " + url)
                .isTrue();
    }
}

