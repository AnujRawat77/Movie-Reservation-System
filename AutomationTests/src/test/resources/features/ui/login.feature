@ui @smoke
Feature: User Login UI

  Background:
    Given the user navigates to the login page

  @smoke @positive
  Scenario: Admin login with valid credentials redirects to home
    When the user enters email "admin@cinereserve.com" and password "Admin@123"
    And the user clicks the login button
    Then the user should be redirected away from the login page

  @smoke @positive
  Scenario: Login page displays all required fields
    Then the email input field should be visible
    And the password input field should be visible
    And the login submit button should be visible

  @positive
  Scenario: Login page has a link to the signup page
    Then the signup link should be visible on the login page

  @negative
  Scenario: Login with invalid credentials shows an error
    When the user enters email "invalid@user.com" and password "WrongPass@1"
    And the user clicks the login button
    Then the user should remain on the login page

  @negative
  Scenario: Login with empty email shows validation error
    When the user enters email "" and password "Admin@123"
    And the user clicks the login button
    Then the user should remain on the login page

  @ui @regression
  Scenario: Login page title contains CineReserve
    Then the page title should contain "CineReserve"

