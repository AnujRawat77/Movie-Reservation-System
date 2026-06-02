package com.cinereserve.ui.tests;

import com.cinereserve.ui.base.BaseUITest;
import com.cinereserve.ui.config.UIConfig;
import com.cinereserve.ui.pages.LoginPage;
import com.cinereserve.ui.pages.MoviesPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium TestNG UI tests — Login Page
 * Tests use headless Chrome by default (configurable via test.properties).
 */
@Epic("UI Tests")
@Feature("Login Page")
public class LoginUITest extends BaseUITest {

    @Test(priority = 1, description = "Login page loads successfully")
    @Story("Page Load")
    @Severity(SeverityLevel.BLOCKER)
    public void loginPage_loadsSuccessfully() {
        LoginPage page = new LoginPage(driver);
        page.open();
        assertThat(page.isEmailFieldVisible()).as("Email field should be visible").isTrue();
        assertThat(page.isPasswordFieldVisible()).as("Password field should be visible").isTrue();
    }

    @Test(priority = 2, description = "Login page title contains CineReserve")
    @Story("Page Load")
    @Severity(SeverityLevel.NORMAL)
    public void loginPage_titleContainsCineReserve() {
        LoginPage page = new LoginPage(driver);
        page.open();
        String title = page.getPageTitle();
        assertThat(title).as("Page title should mention CineReserve").containsIgnoringCase("CineReserve");
    }

    @Test(priority = 3, description = "Submit button is present and enabled")
    @Story("Form Elements")
    @Severity(SeverityLevel.CRITICAL)
    public void loginPage_submitButtonIsEnabled() {
        LoginPage page = new LoginPage(driver);
        page.open();
        assertThat(page.isSubmitButtonEnabled()).as("Submit button should be enabled").isTrue();
    }

    @Test(priority = 4, description = "Signup link is visible on login page")
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    public void loginPage_signupLinkIsVisible() {
        LoginPage page = new LoginPage(driver);
        page.open();
        assertThat(page.isSignupLinkVisible()).as("Signup link should be visible").isTrue();
    }

    @Test(priority = 5, description = "Admin login redirects away from login page")
    @Story("Successful Login")
    @Severity(SeverityLevel.BLOCKER)
    public void adminLogin_redirectsFromLoginPage() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.loginWith("admin@cinereserve.com", "Admin@123");
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        String url = driver.getCurrentUrl();
        assertThat(url).as("Should be redirected from login page after admin login")
                .doesNotContain("/login");
    }

    @Test(priority = 6, description = "Login page URL is correct")
    @Story("Page Load")
    @Severity(SeverityLevel.NORMAL)
    public void loginPage_urlIsCorrect() {
        navigateTo("/login");
        assertThat(driver.getCurrentUrl()).as("URL should contain /login").contains("/login");
    }

    @Test(priority = 7, description = "Login with wrong password stays on login page")
    @Story("Failed Login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithWrongPassword_staysOnLoginPage() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.loginWith("admin@cinereserve.com", "WrongPassword123!");
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        String url = driver.getCurrentUrl();
        assertThat(url).as("Should remain on login or get error").contains("/login");
    }

    @Test(priority = 8, description = "Navigating to signup from login page works")
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    public void loginPage_signupNavigation_works() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.clickSignupLink();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        assertThat(driver.getCurrentUrl()).as("Should navigate to signup").contains("/signup");
    }
}

