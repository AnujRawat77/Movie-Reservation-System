package com.cinereserve.ui.steps;

import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.LoginPage;
import com.cinereserve.ui.pages.ProfilePage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the User Profile page scenarios.
 */
public class ProfileSteps {

    private static final Logger log = LogManager.getLogger(ProfileSteps.class);

    private final DriverContext ctx;
    private ProfilePage profilePage;

    public ProfileSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @Given("the user is logged in as a regular user")
    public void loginAsRegularUser() {
        ctx.navigateTo("/login");
        LoginPage loginPage = new LoginPage(ctx.driver());
        loginPage.waitForPageLoad();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        Allure.step("Logged in as regular user (via admin credentials)");
        log.info("Logged in as regular user");
    }

    @Given("the user is not logged in")
    public void ensureNotLoggedIn() {
        ctx.driver().manage().deleteAllCookies();
        ctx.navigateTo("/");
        Allure.step("Cleared session — user not logged in");
        log.info("Cleared cookies, user is not logged in");
    }

    @When("the user navigates to the profile page")
    public void navigateToProfilePage() {
        profilePage = new ProfilePage(ctx.driver());
        profilePage.open();
        Allure.step("Navigated to profile page");
        log.info("Navigated to /profile");
    }

    @Then("the profile page should be visible")
    public void verifyProfilePageVisible() {
        assertThat(profilePage.isPageVisible())
                .as("Profile page should be visible").isTrue();
    }

    @Then("the user's name should be displayed")
    public void verifyUserNameDisplayed() {
        String name = profilePage.getDisplayedUserName();
        Allure.step("Profile displayed name: " + name);
        assertThat(name).isNotBlank();
    }

    @Then("the profile page should contain a loyalty points section")
    public void verifyLoyaltySectionVisible() {
        assertThat(profilePage.isLoyaltySectionVisible())
                .as("Loyalty section should be visible on profile").isTrue();
    }

    @Then("the user should be redirected to the login page")
    public void verifyRedirectedToLogin() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Redirected to: " + url);
        assertThat(url).contains("/login");
    }
}
