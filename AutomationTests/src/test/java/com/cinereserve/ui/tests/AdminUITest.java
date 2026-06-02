package com.cinereserve.ui.tests;

import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.ui.base.BaseUITest;
import com.cinereserve.ui.pages.AdminPage;
import com.cinereserve.ui.pages.LoginPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium TestNG UI tests — Admin area.
 */
@Epic("UI Tests")
@Feature("Admin Dashboard")
public class AdminUITest extends BaseUITest {

    @Test(priority = 1, description = "Unauthenticated access to admin is blocked or redirected")
    @Story("Access Control")
    @Severity(SeverityLevel.BLOCKER)
    public void adminPage_unauthenticated_isBlockedOrRedirected() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.openDashboard();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        String url = driver.getCurrentUrl();
        boolean blocked = url.contains("/login") || !url.contains("/admin");
        assertThat(blocked).as("Unauthenticated admin access should be blocked, URL: " + url).isTrue();
    }

    @Test(priority = 2, description = "Admin can log in and reach admin dashboard")
    @Story("Admin Login")
    @Severity(SeverityLevel.BLOCKER)
    public void adminLogin_accessesDashboard() {
        // Login via UI
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Navigate to admin
        AdminPage adminPage = new AdminPage(driver);
        adminPage.openDashboard();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        assertThat(url).as("Admin should be able to access admin dashboard after login")
                .doesNotContain("/login");
    }

    @Test(priority = 3, description = "Admin movies page is reachable after login")
    @Story("Admin Pages")
    @Severity(SeverityLevel.CRITICAL)
    public void adminMoviesPage_isReachable() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}

        AdminPage adminPage = new AdminPage(driver);
        adminPage.openMovies();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        assertThat(url).as("Admin movies page should load").doesNotContain("/login");
    }

    @Test(priority = 4, description = "Admin users page is reachable after login")
    @Story("Admin Pages")
    @Severity(SeverityLevel.CRITICAL)
    public void adminUsersPage_isReachable() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}

        AdminPage adminPage = new AdminPage(driver);
        adminPage.openUsers();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        assertThat(url).as("Admin users page should load").doesNotContain("/login");
    }

    @Test(priority = 5, description = "Admin halls page is reachable after login")
    @Story("Admin Pages")
    @Severity(SeverityLevel.NORMAL)
    public void adminHallsPage_isReachable() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}

        AdminPage adminPage = new AdminPage(driver);
        adminPage.openHalls();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        assertThat(url).as("Admin halls page should load").doesNotContain("/login");
    }

    @Test(priority = 6, description = "Admin reports page is reachable after login")
    @Story("Admin Pages")
    @Severity(SeverityLevel.NORMAL)
    public void adminReportsPage_isReachable() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD);
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}

        AdminPage adminPage = new AdminPage(driver);
        adminPage.openReports();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        assertThat(url).as("Admin reports page should load").doesNotContain("/login");
    }
}

