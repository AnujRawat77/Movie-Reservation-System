@ui @regression
Feature: User Signup UI

  @smoke @positive
  Scenario: Signup page displays required form fields
    Given the user navigates to the signup page
    Then the email input field should be visible on signup
    And the password input field should be visible on signup

  @positive
  Scenario: Signup page has a link back to login
    Given the user navigates to the signup page
    Then the login link should be visible on the signup page

  @regression @positive
  Scenario: Signup page title contains CineReserve
    Given the user navigates to the signup page
    Then the page title should contain "CineReserve"

  @positive
  Scenario: Clicking login link from signup navigates to login
    Given the user navigates to the signup page
    When the user clicks the login link on signup
    Then the user should be on the login page

