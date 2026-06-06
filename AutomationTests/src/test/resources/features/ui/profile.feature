@ui @regression
Feature: User Profile Page

  @smoke @positive
  Scenario: Logged-in user can view profile page
    Given the user is logged in as a regular user
    When the user navigates to the profile page
    Then the profile page should be visible
    And the user's name should be displayed

  @positive
  Scenario: Unauthenticated user is redirected from profile page
    Given the user is not logged in
    When the user navigates to the profile page
    Then the user should be redirected to the login page

  @positive
  Scenario: Profile page shows loyalty points link
    Given the user is logged in as a regular user
    When the user navigates to the profile page
    Then the profile page should contain a loyalty points section
