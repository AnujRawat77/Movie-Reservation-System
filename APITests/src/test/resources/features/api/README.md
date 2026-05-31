# API BDD Feature Files

Place Cucumber `.feature` files for API-level BDD scenarios in this directory.

> **Note:** The existing API tests (RestAssured + TestNG) live under
> `src/test/java/com/cinereserve/api/tests/`. This directory is for any
> future Cucumber-style API BDD scenarios that you want to express in Gherkin.

## Naming Convention

```
features/api/
├── auth/
│   ├── register.feature
│   └── login.feature
├── movies/
│   └── movie_crud.feature
├── reservations/
│   └── book_reservation.feature
└── ...
```

## Feature File Template

```gherkin
@api @smoke
Feature: Movie Listing API
  As a consumer of the CineReserve API
  I want to retrieve a list of movies
  So that I can display them to users

  Scenario: Get all movies returns HTTP 200
    Given the API is running at the configured base URL
    When  I send GET "/api/movies"
    Then  the response status code should be 200
    And   the response body should contain a "data" array
```

## Dedicated API BDD Runner (future)

When you add API Cucumber scenarios, create a runner:

```java
@CucumberOptions(
    features = "src/test/resources/features/api",
    glue     = { "com.cinereserve.api.bdd.steps", "com.cinereserve.api.bdd.hooks" },
    plugin   = { "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm", "pretty" }
)
public class CucumberAPIRunner extends AbstractTestNGCucumberTests { }
```

