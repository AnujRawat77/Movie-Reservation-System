@ui @regression
Feature: Booking Success Page

  @positive
  Scenario: Booking success page loads with a valid reservation id
    Given the user is logged in as a regular user
    When the user navigates to the booking success page with id 1
    Then the booking success page should be visible

  @positive
  Scenario: Booking success page shows booking reference or confirmation
    Given the user is logged in as a regular user
    When the user navigates to the booking success page with id 1
    Then the page should display confirmation content

  @positive
  Scenario: Booking success page has navigation back to bookings
    Given the user is logged in as a regular user
    When the user navigates to the booking success page with id 1
    Then there should be a link or button to view bookings
