@ui @regression
Feature: My Bookings and Booking Detail Page

  Background:
    Given the user is logged in as a valid user
    And the user navigates to the My Bookings page

  @ui @smoke @positive
  Scenario: My Bookings page loads without errors
    Then the user should be on the My Bookings page
    And the bookings page should load without errors

  @ui @positive
  Scenario: View Details link navigates to the booking detail page
    When the user clicks View Details on the first booking
    Then the user should be on the booking detail page

  @ui @positive
  Scenario: Booking detail page displays all required information
    When the user clicks View Details on the first booking
    Then the user should be on the booking detail page
    And the detail page should display the movie title
    And the detail page should display the booking status
    And the detail page should display the total amount
    And the detail page should display the booked seats

  @ui @positive
  Scenario: Download Receipt button is visible on the booking detail page
    When the user clicks View Details on the first booking
    Then the user should be on the booking detail page
    And the Download Receipt button should be visible on the detail page

  @ui @positive
  Scenario: Back link on the detail page returns to My Bookings
    When the user clicks View Details on the first booking
    And the user clicks the back link on the detail page
    Then the user should be back on the My Bookings page
