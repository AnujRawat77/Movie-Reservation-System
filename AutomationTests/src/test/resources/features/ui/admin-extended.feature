@ui @regression
Feature: Admin Extended Pages

  @regression @positive
  Scenario: Admin genres page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin genres page
    Then the admin page should load without errors

  @regression @positive
  Scenario: Admin halls page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin halls page
    Then the admin page should load without errors

  @regression @positive
  Scenario: Admin showtimes page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin showtimes page
    Then the admin page should load without errors

  @regression @positive
  Scenario: Admin reservations page is accessible after login
    Given the user is logged in as admin
    When the user navigates to the admin reservations page
    Then the admin page should load without errors
