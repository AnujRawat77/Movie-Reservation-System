package com.cinereserve.ui.steps;

import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.LoginPage;
import com.cinereserve.ui.pages.SignupPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Signup flow — form submission and verification.
 */
public class SignupFlowSteps {

    private static final Logger log = LogManager.getLogger(SignupFlowSteps.class);

    private final DriverContext ctx;
    private SignupPage signupPage;

    public SignupFlowSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @When("the user fills in the signup form with name {string}, email {string} and password {string}")
    public void fillSignupForm(String name, String email, String password) {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        signupPage.enterName(name);
        signupPage.enterEmail(email);
        signupPage.enterPassword(password);
        Allure.step("Filled signup form: name=" + name + ", email=" + email);
        log.info("Filled signup form with name={}, email={}", name, email);
    }

    @When("the user submits the signup form")
    public void submitSignupForm() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        signupPage.clickSignup();
        Allure.step("Submitted signup form");
        log.info("Clicked signup submit button");
    }

    @When("the user submits the signup form without filling in any fields")
    public void submitEmptySignupForm() {
        if (signupPage == null) signupPage = new SignupPage(ctx.driver());
        signupPage.clickSignup();
        Allure.step("Submitted empty signup form");
        log.info("Submitted signup form without any data");
    }

    @Then("the user should be redirected away from the signup page")
    public void verifyRedirectedFromSignup() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String currentUrl = ctx.driver().getCurrentUrl();
        Allure.step("Current URL after signup: " + currentUrl);
        log.info("URL after signup attempt: {}", currentUrl);
        assertThat(currentUrl).doesNotEndWith("/signup");
    }

    @Then("the signup form should not navigate away")
    public void verifyStillOnSignupPage() {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        String currentUrl = ctx.driver().getCurrentUrl();
        Allure.step("Stayed on URL: " + currentUrl);
        log.info("URL after empty submit: {}", currentUrl);
        assertThat(currentUrl).contains("/signup");
    }
}
