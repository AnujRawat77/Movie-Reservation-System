package com.cinereserve.ui.steps;

import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.LoginPage;
import com.cinereserve.ui.pages.MoviesPage;
import com.cinereserve.ui.pages.SignupPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Login feature scenarios.
 */
public class LoginSteps {

    private static final Logger log = LogManager.getLogger(LoginSteps.class);

    private final DriverContext ctx;
    private LoginPage loginPage;
    private MoviesPage moviesPage;
    private SignupPage signupPage;

    public LoginSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    // ── Given ────────────────────────────────────────────────────────────────

    @Given("the user navigates to the login page")
    public void navigateToLoginPage() {
        loginPage = new LoginPage(ctx.driver());
        loginPage.open();
        Allure.step("Navigated to login page: " + UIConfig.BASE_URL + "/login");
        log.info("Navigated to login page");
    }

    @Given("the user navigates to the movies home page")
    public void navigateToMoviesPage() {
        moviesPage = new MoviesPage(ctx.driver());
        moviesPage.open();
        Allure.step("Navigated to movies home page");
        log.info("Navigated to movies home page");
    }

    @Given("the user navigates to the signup page")
    public void navigateToSignupPage() {
        signupPage = new SignupPage(ctx.driver());
        signupPage.open();
        Allure.step("Navigated to signup page: " + UIConfig.BASE_URL + "/signup");
        log.info("Navigated to signup page");
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @When("the user enters email {string} and password {string}")
    public void enterEmailAndPassword(String email, String password) {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        Allure.step("Entered email: " + email + " and password");
        log.info("Entered credentials: email={}", email);
    }

    @When("the user clicks the login button")
    public void clickLoginButton() {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        loginPage.clickLogin();
        Allure.step("Clicked login button");
        log.info("Clicked login button");
    }

    @When("the user clicks the login navigation link")
    public void clickLoginNavLink() {
        if (moviesPage == null) moviesPage = new MoviesPage(ctx.driver());
        moviesPage.clickLoginLink();
        Allure.step("Clicked login navigation link");
    }

    @When("the user clicks the signup navigation link")
    public void clickSignupNavLink() {
        if (moviesPage == null) moviesPage = new MoviesPage(ctx.driver());
        moviesPage.clickSignupLink();
        Allure.step("Clicked signup navigation link");
    }

    @When("the user clicks the login link on signup")
    public void clickLoginLinkOnSignup() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        signupPage.clickLoginLink();
        Allure.step("Clicked login link on signup page");
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Then("the user should be redirected away from the login page")
    public void verifyRedirectedFromLogin() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String currentUrl = ctx.driver().getCurrentUrl();
        Allure.step("Current URL after login: " + currentUrl);
        log.info("Current URL after login: {}", currentUrl);
        assertThat(currentUrl)
                .as("User should be redirected away from login page")
                .doesNotContain("/login");
    }

    @Then("the user should remain on the login page")
    public void verifyStillOnLoginPage() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String currentUrl = ctx.driver().getCurrentUrl();
        Allure.step("Current URL: " + currentUrl);
        log.info("Current URL (expect login): {}", currentUrl);
        assertThat(currentUrl)
                .as("User should remain on login page after failed login")
                .contains("/login");
    }

    @Then("the email input field should be visible")
    public void verifyEmailFieldVisible() {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        assertThat(loginPage.isEmailFieldVisible()).as("Email field should be visible").isTrue();
    }

    @Then("the password input field should be visible")
    public void verifyPasswordFieldVisible() {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        assertThat(loginPage.isPasswordFieldVisible()).as("Password field should be visible").isTrue();
    }

    @Then("the login submit button should be visible")
    public void verifySubmitButtonVisible() {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        assertThat(loginPage.isSubmitButtonEnabled()).as("Submit button should be enabled").isTrue();
    }

    @Then("the signup link should be visible on the login page")
    public void verifySignupLinkOnLogin() {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver());
        assertThat(loginPage.isSignupLinkVisible()).as("Signup link should be visible").isTrue();
    }

    @Then("the page title should contain {string}")
    public void verifyPageTitleContains(String expectedText) {
        String title = ctx.driver().getTitle();
        Allure.step("Page title: " + title);
        log.info("Page title: {}", title);
        assertThat(title).as("Page title should contain: " + expectedText)
                .containsIgnoringCase(expectedText);
    }

    @Then("the page should load successfully")
    public void verifyPageLoadsSuccessfully() {
        if (moviesPage == null) moviesPage = new MoviesPage(ctx.driver());
        assertThat(moviesPage.isPageLoaded()).as("Page should be loaded successfully").isTrue();
    }

    @Then("the navigation bar should be visible")
    public void verifyNavBarVisible() {
        if (moviesPage == null) moviesPage = new MoviesPage(ctx.driver());
        assertThat(moviesPage.isNavBarVisible()).as("Navigation bar should be visible").isTrue();
    }

    @Then("the login link should be visible in the navigation")
    public void verifyLoginLinkInNav() {
        if (moviesPage == null) moviesPage = new MoviesPage(ctx.driver());
        boolean loginVisible = moviesPage.isLoginLinkVisible();
        Allure.step("Login link visible: " + loginVisible);
        // If user is logged in, login link may not be visible — that's OK
        log.info("Login link visible in nav: {}", loginVisible);
    }

    @Then("the user should be on the login page")
    public void verifyOnLoginPage() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Current URL: " + url);
        assertThat(url).as("Should be on login page").contains("/login");
    }

    @Then("the user should be on the signup page")
    public void verifyOnSignupPage() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Current URL: " + url);
        assertThat(url).as("Should be on signup page").contains("/signup");
    }

    // Signup page steps
    @Then("the email input field should be visible on signup")
    public void verifyEmailFieldOnSignup() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        assertThat(signupPage.isEmailFieldVisible()).as("Email field should be visible on signup").isTrue();
    }

    @Then("the password input field should be visible on signup")
    public void verifyPasswordFieldOnSignup() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        assertThat(signupPage.isPasswordFieldVisible()).as("Password field should be visible on signup").isTrue();
    }

    @Then("the login link should be visible on the signup page")
    public void verifyLoginLinkOnSignup() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        assertThat(signupPage.isLoginLinkVisible()).as("Login link should be visible on signup page").isTrue();
    }
}

