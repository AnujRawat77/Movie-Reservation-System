@ui @regression
Feature: Signup Form Submission

  @smoke @positive
  Scenario: User can complete signup with valid data
    Given the user navigates to the signup page
    When the user fills in the signup form with name "Test User", email "testuser@example.com" and password "secret123"
    And the user submits the signup form
    Then the user should be redirected away from the signup page

  @positive
  Scenario: Signup with missing required fields shows validation
    Given the user navigates to the signup page
    When the user submits the signup form without filling in any fields
    Then the signup form should not navigate away
