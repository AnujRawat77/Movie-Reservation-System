@ui @smoke
Feature: Movies List UI

  @smoke @positive
  Scenario: Home page loads and displays movies
    Given the user navigates to the movies home page
    Then the page should load successfully
    And the navigation bar should be visible

  @smoke @positive
  Scenario: Home page shows login and signup navigation links
    Given the user navigates to the movies home page
    Then the login link should be visible in the navigation

  @positive
  Scenario: Home page title contains CineReserve
    Given the user navigates to the movies home page
    Then the page title should contain "CineReserve"

  @regression @positive
  Scenario: Navigating to login page from home works
    Given the user navigates to the movies home page
    When the user clicks the login navigation link
    Then the user should be on the login page

  @regression @positive
  Scenario: Navigating to signup page from home works
    Given the user navigates to the movies home page
    When the user clicks the signup navigation link
    Then the user should be on the signup page

