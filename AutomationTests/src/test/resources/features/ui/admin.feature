@ui @regression
Feature: Admin Dashboard UI

  @regression @security
  Scenario: Unauthenticated user is redirected from admin dashboard
    Given the user navigates to the admin dashboard without authentication
    Then the user should not be able to access the admin area

  @regression @security
  Scenario: Admin login grants access to admin area
    Given the user is logged in as admin
    When the user navigates to the admin dashboard
    Then the admin dashboard should be accessible

  @regression @positive
  Scenario: Admin movies page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin movies page
    Then the admin page should load without errors

  @regression @positive
  Scenario: Admin users page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin users page
    Then the admin page should load without errors

  @regression @positive
  Scenario: Admin reports page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin reports page
    Then the admin page should load without errors

