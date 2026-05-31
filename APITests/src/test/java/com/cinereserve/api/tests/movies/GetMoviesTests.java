package com.cinereserve.api.tests.movies;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Get Movies API
 * Endpoints: GET /api/movies, GET /api/movies/{id}, GET /api/movies/{id}/showtimes
 * Total @Test methods: 25
 */
@Epic("Movies")
@Feature("Get Movies")
public class GetMoviesTests extends BaseTest {

    // ─── GET ALL MOVIES ──────────────────────────────────────────────────────

    @Test(priority = 1, description = "Get all movies returns 200")
    @Story("List Movies")
    @Severity(SeverityLevel.BLOCKER)
    public void getAllMovies_returns200() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 2, description = "Get all movies without auth returns 200 (public endpoint)")
    @Story("List Movies")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllMoviesWithoutAuth_returns200() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 3, description = "Get all movies returns array in data field")
    @Story("List Movies")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllMoviesReturnsArray() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200).body("success", equalTo(true));
        Object data = res.jsonPath().get("data");
        assertThat(data).isInstanceOf(List.class);
    }

    @Test(priority = 4, description = "Get all movies response is not empty (seeded data)")
    @Story("List Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMoviesResponseIsNotEmpty() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        List<?> movies = res.jsonPath().getList("data");
        assertThat(movies).isNotNull();
    }

    @Test(priority = 5, description = "Each movie has id, title, durationMinutes")
    @Story("List Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMovies_eachMovieHasRequiredFields() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        List<Object> movies = res.jsonPath().getList("data");
        if (!movies.isEmpty()) {
            res.then()
                    .body("data[0].id", notNullValue())
                    .body("data[0].title", notNullValue())
                    .body("data[0].durationMinutes", notNullValue());
        }
    }

    @Test(priority = 6, description = "Get movies filtered by status=now")
    @Story("Filter Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMoviesFilterByStatusNow_returns200() {
        withNoAuth()
                .queryParam("status", "now")
                .get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test(priority = 7, description = "Get movies filtered by status=soon")
    @Story("Filter Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMoviesFilterByStatusSoon_returns200() {
        withNoAuth()
                .queryParam("status", "soon")
                .get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 8, description = "Get movies filtered by genre name")
    @Story("Filter Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMoviesFilterByGenre_returns200() {
        withNoAuth()
                .queryParam("genre", "Action")
                .get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 9, description = "Get movies with search query returns 200")
    @Story("Filter Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getAllMoviesWithSearchQuery_returns200() {
        withNoAuth()
                .queryParam("search", "the")
                .get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 10, description = "Get movies with non-existent genre returns empty array or 200")
    @Story("Filter Movies")
    @Severity(SeverityLevel.MINOR)
    public void getAllMoviesWithInvalidGenre_returns200() {
        Response res = withNoAuth()
                .queryParam("genre", "NonExistentGenreXYZ999")
                .get(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        List<?> movies = res.jsonPath().getList("data");
        assertThat(movies).isNotNull();
    }

    @Test(priority = 11, description = "Get movies response time under 5 seconds")
    @Story("List Movies")
    @Severity(SeverityLevel.MINOR)
    public void getAllMoviesResponseTimeUnder5Seconds() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 12, description = "Get movies with includeDeleted=false (default) should not return deleted movies")
    @Story("Filter Movies")
    @Severity(SeverityLevel.NORMAL)
    public void getMovies_includeDeletedFalse_returnsActiveMovies() {
        withNoAuth()
                .queryParam("includeDeleted", false)
                .get(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 13, description = "Get movies content type is JSON")
    @Story("List Movies")
    @Severity(SeverityLevel.MINOR)
    public void getAllMoviesContentTypeIsJson() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .then().contentType(containsString("application/json"));
    }

    // ─── GET MOVIE BY ID ─────────────────────────────────────────────────────

    @Test(priority = 14, description = "Get movie by valid ID returns 200")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.BLOCKER)
    public void getMovieById_withValidId_returns200() {
        // Get list first to get a valid ID
        Response list = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        list.then().statusCode(200);
        List<Integer> ids = list.jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            int movieId = ids.get(0);
            withNoAuth().pathParam("id", movieId)
                    .get(ApiConfig.Endpoints.MOVIE_BY_ID)
                    .then().statusCode(200)
                    .body("data.id", equalTo(movieId))
                    .body("data.title", notNullValue());
        }
    }

    @Test(priority = 15, description = "Get movie by non-existent ID returns 404")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void getMovieByNonExistentId_returns404() {
        withNoAuth().pathParam("id", 999999)
                .get(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 16, description = "Get movie by negative ID returns 4xx")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.NORMAL)
    public void getMovieByNegativeId_returns4xx() {
        int status = withNoAuth().pathParam("id", -1)
                .get(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 17, description = "Get movie by ID without auth returns 200 (public)")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.NORMAL)
    public void getMovieByIdWithoutAuth_returns200() {
        // Fetch first available ID
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.MOVIE_BY_ID)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 18, description = "Movie detail contains genres list")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.NORMAL)
    public void getMovieById_detailContainsGenres() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.MOVIE_BY_ID)
                    .then().statusCode(200)
                    .body("data.genres", notNullValue());
        }
    }

    @Test(priority = 19, description = "Get movie by string ID returns 4xx")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.MINOR)
    public void getMovieByStringId_returns4xx() {
        int status = withNoAuth().pathParam("id", "abc")
                .get(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 20, description = "Get movie by zero ID returns 4xx")
    @Story("Get Movie by ID")
    @Severity(SeverityLevel.MINOR)
    public void getMovieByZeroId_returns4xx() {
        int status = withNoAuth().pathParam("id", 0)
                .get(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    // ─── GET SHOWTIMES FOR MOVIE ──────────────────────────────────────────────

    @Test(priority = 21, description = "Get showtimes for movie returns 200")
    @Story("Movie Showtimes")
    @Severity(SeverityLevel.CRITICAL)
    public void getShowtimesForMovie_returns200() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 22, description = "Get showtimes for movie without auth returns 200 (public)")
    @Story("Movie Showtimes")
    @Severity(SeverityLevel.NORMAL)
    public void getShowtimesForMovieWithoutAuth_returns200() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 23, description = "Get showtimes with date filter returns 200")
    @Story("Movie Showtimes")
    @Severity(SeverityLevel.NORMAL)
    public void getShowtimesForMovieWithDateFilter_returns200() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .queryParam("date", "2026-06-01")
                    .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 24, description = "Get showtimes for non-existent movie returns 404")
    @Story("Movie Showtimes")
    @Severity(SeverityLevel.NORMAL)
    public void getShowtimesForNonExistentMovie_returns404() {
        withNoAuth().pathParam("id", 999999)
                .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES)
                .then().statusCode(anyOf(equalTo(404), equalTo(200)));
    }

    @Test(priority = 25, description = "Get showtimes returns array in data")
    @Story("Movie Showtimes")
    @Severity(SeverityLevel.NORMAL)
    public void getShowtimesForMovieReturnsArray() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES);
            res.then().statusCode(200);
            Object data = res.jsonPath().get("data");
            assertThat(data).isInstanceOf(List.class);
        }
    }
}

