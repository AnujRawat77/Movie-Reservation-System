# UI Feature Files

Place Cucumber `.feature` files for UI / end-to-end BDD scenarios in this directory.

## Naming Convention

```
features/ui/
├── auth/
│   ├── login.feature
│   └── register.feature
├── movies/
│   ├── browse_movies.feature
│   └── movie_detail.feature
├── reservation/
│   └── book_seat.feature
└── ...
```

## Feature File Template

```gherkin
@ui @smoke
Feature: User Login
  As a registered user
  I want to log in to CineReserve
  So that I can browse movies and make reservations

  Background:
    Given the application is running

  Scenario: Successful login with valid credentials
    Given the user is on the login page
    When  the user logs in with "user@example.com" and "Password@123"
    Then  the user should be redirected to the home page
    And   the navigation bar should show the user's name

  Scenario: Login fails with wrong password
    Given the user is on the login page
    When  the user logs in with "user@example.com" and "wrongpassword"
    Then  an error message "Invalid credentials" should be displayed
```

## Running BDD Tests

```bash
# All BDD UI scenarios
mvn test -P bdd-tests

# Only @smoke tagged scenarios
mvn test -P bdd-tests -Dcucumber.filter.tags="@smoke"

# Specific feature file
mvn test -P bdd-tests -Dcucumber.features="src/test/resources/features/ui/auth/login.feature"
```

