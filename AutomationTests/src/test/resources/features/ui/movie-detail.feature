@ui @regression
Feature: Movie Detail Page Interactions

  @smoke @positive
  Scenario: Movie detail page loads for a valid movie
    Given a movie exists in the system with id 1
    When the user navigates to the movie detail page for movie 1
    Then the movie detail page should be visible

  @positive
  Scenario: Movie detail page displays showtimes section
    Given a movie exists in the system with id 1
    When the user navigates to the movie detail page for movie 1
    Then the page should display a showtimes or booking section

  @positive
  Scenario: Movie detail page has a book now or select showtime option
    Given the user is logged in as a regular user
    When the user navigates to the movie detail page for movie 1
    Then there should be a booking call-to-action on the page
