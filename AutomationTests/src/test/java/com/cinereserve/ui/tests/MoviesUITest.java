package com.cinereserve.ui.tests;

import com.cinereserve.ui.base.BaseUITest;
import com.cinereserve.ui.pages.MoviesPage;
import com.cinereserve.ui.pages.SignupPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium TestNG UI tests — Movies & Signup pages.
 */
@Epic("UI Tests")
@Feature("Movies & Navigation")
public class MoviesUITest extends BaseUITest {

    @Test(priority = 1, description = "Home page loads and page state is complete")
    @Story("Home Page")
    @Severity(SeverityLevel.BLOCKER)
    public void homePage_loadsSuccessfully() {
        MoviesPage page = new MoviesPage(driver);
        page.open();
        assertThat(page.isPageLoaded()).as("Home page should be fully loaded").isTrue();
    }

    @Test(priority = 2, description = "Home page title contains CineReserve")
    @Story("Home Page")
    @Severity(SeverityLevel.NORMAL)
    public void homePage_titleContainsCineReserve() {
        MoviesPage page = new MoviesPage(driver);
        page.open();
        String title = page.getPageTitle();
        assertThat(title).as("Home page title should mention CineReserve").containsIgnoringCase("CineReserve");
    }

    @Test(priority = 3, description = "Navigation bar is visible on home page")
    @Story("Navigation")
    @Severity(SeverityLevel.CRITICAL)
    public void homePage_navBarIsVisible() {
        MoviesPage page = new MoviesPage(driver);
        page.open();
        assertThat(page.isNavBarVisible()).as("Navigation bar should be visible").isTrue();
    }

    @Test(priority = 4, description = "Clicking login link navigates to login page")
    @Story("Navigation")
    @Severity(SeverityLevel.CRITICAL)
    public void homePage_loginLinkNavigatesToLogin() {
        MoviesPage page = new MoviesPage(driver);
        page.open();
        if (page.isLoginLinkVisible()) {
            page.clickLoginLink();
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            assertThat(driver.getCurrentUrl()).as("Should navigate to login").contains("/login");
        }
    }

    @Test(priority = 5, description = "Home page URL is the root")
    @Story("Home Page")
    @Severity(SeverityLevel.NORMAL)
    public void homePage_urlIsRoot() {
        navigateTo("/");
        String url = driver.getCurrentUrl();
        assertThat(url).as("Home URL should be root or /movies").satisfiesAnyOf(
                u -> assertThat(u).endsWith("/"),
                u -> assertThat(u).contains("/movies")
        );
    }

    @Test(priority = 6, description = "Signup page renders with email and password fields")
    @Story("Signup Page")
    @Severity(SeverityLevel.BLOCKER)
    public void signupPage_rendersFormFields() {
        SignupPage page = new SignupPage(driver);
        page.open();
        assertThat(page.isEmailFieldVisible()).as("Email field should be visible on signup").isTrue();
        assertThat(page.isPasswordFieldVisible()).as("Password field should be visible on signup").isTrue();
    }

    @Test(priority = 7, description = "Signup page has link back to login")
    @Story("Signup Page")
    @Severity(SeverityLevel.NORMAL)
    public void signupPage_hasLoginLink() {
        SignupPage page = new SignupPage(driver);
        page.open();
        assertThat(page.isLoginLinkVisible()).as("Login link should be visible on signup page").isTrue();
    }

    @Test(priority = 8, description = "Signup page title contains CineReserve")
    @Story("Signup Page")
    @Severity(SeverityLevel.NORMAL)
    public void signupPage_titleContainsCineReserve() {
        SignupPage page = new SignupPage(driver);
        page.open();
        assertThat(driver.getTitle()).containsIgnoringCase("CineReserve");
    }

    @Test(priority = 9, description = "Navigating login link from signup goes to login")
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    public void signupPage_loginLinkNavigation() {
        SignupPage page = new SignupPage(driver);
        page.open();
        if (page.isLoginLinkVisible()) {
            page.clickLoginLink();
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            assertThat(driver.getCurrentUrl()).as("Should navigate to login").contains("/login");
        }
    }
}

