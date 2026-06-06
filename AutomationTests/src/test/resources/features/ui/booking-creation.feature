@ui @regression
Feature: Booking Creation Flow

  @smoke @positive
  Scenario: Booking page loads for a valid showtime
    Given a showtime exists with id 1
    And the user is logged in as a regular user
    When the user navigates to the booking page for showtime 1
    Then the booking page should be visible

  @positive
  Scenario: Seat selection is displayed on booking page
    Given a showtime exists with id 1
    And the user is logged in as a regular user
    When the user navigates to the booking page for showtime 1
    Then the seat grid or seat map should be visible

  @positive
  Scenario: Unauthenticated user is redirected from booking page
    Given the user is not logged in
    When the user navigates to the booking page for showtime 1
    Then the user should be redirected to the login page
