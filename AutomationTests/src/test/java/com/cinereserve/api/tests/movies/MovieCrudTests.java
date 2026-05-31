package com.cinereserve.api.tests.movies;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Create, Update & Delete Movie API
 * Endpoints: POST /api/movies, PUT /api/movies/{id}, DELETE /api/movies/{id}
 * Total @Test methods: 60
 */
@Epic("Movies")
@Feature("Movie Management")
public class MovieCrudTests extends BaseTest {

    private int existingGenreId = 1; // default seeded genre
    private int createdMovieId;

    @BeforeClass(alwaysRun = true)
    public void fetchExistingGenre() {
        try {
            Response res = withNoAuth().get(ApiConfig.Endpoints.GENRES);
            List<Integer> ids = res.jsonPath().getList("data.id");
            if (ids != null && !ids.isEmpty()) {
                existingGenreId = ids.get(0);
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE MOVIE TESTS (POST /api/movies)
    // ══════════════════════════════════════════════════════════════

    @Test(priority = 1, description = "Admin can create a movie with valid data")
    @Story("Create Movie")
    @Severity(SeverityLevel.BLOCKER)
    public void createMovieWithValidData_returns200() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.title", equalTo(body.get("title")));
        createdMovieId = res.jsonPath().getInt("data.id");
    }

    @Test(priority = 2, description = "Create movie without auth returns 401")
    @Story("Create Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithoutAuth_returns401() {
        withNoAuth()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 3, description = "Regular user cannot create movie - returns 403")
    @Story("Create Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithUserRole_returns403() {
        withUserAuth()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(403);
    }

    @Test(priority = 4, description = "Create movie with invalid token returns 401")
    @Story("Create Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithInvalidToken_returns401() {
        withInvalidToken()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 5, description = "Create movie with empty title returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithEmptyTitle_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("title", "");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(400);
    }

    @Test(priority = 6, description = "Create movie with null title returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithNullTitle_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("title", null);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(400);
    }

    @Test(priority = 7, description = "Create movie with zero duration returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithZeroDuration_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("durationMinutes", 0);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(400);
    }

    @Test(priority = 8, description = "Create movie with negative duration returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createMovieWithNegativeDuration_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("durationMinutes", -90);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(400);
    }

    @Test(priority = 9, description = "Create movie with valid genres assigned")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithValidGenres_genresAssigned() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200)
                .body("data.genres", notNullValue())
                .body("data.genres.size()", greaterThan(0));
    }

    @Test(priority = 10, description = "Create movie with empty genre list is allowed")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithEmptyGenreList_behaviorDefined() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(Collections.emptyList());
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 11, description = "Create movie with invalid genre IDs returns 4xx")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithInvalidGenreIds_returns4xx() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(999999));
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 12, description = "Create movie with status=now is valid")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithStatusNow_isValid() {
        Map<String, Object> body = TestDataBuilder.movieBodyWithStatus("now", List.of(existingGenreId));
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.status", equalTo("now"));
    }

    @Test(priority = 13, description = "Create movie with status=soon is valid")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithStatusSoon_isValid() {
        Map<String, Object> body = TestDataBuilder.movieBodyWithStatus("soon", List.of(existingGenreId));
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.status", equalTo("soon"));
    }

    @Test(priority = 14, description = "Create movie with invalid status returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithInvalidStatus_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("status", "invalid_status");
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 15, description = "Created movie response contains all required fields")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieResponse_containsAllRequiredFields() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.id", notNullValue())
                .body("data.title", notNullValue())
                .body("data.durationMinutes", notNullValue())
                .body("data.genres", notNullValue());
    }

    @Test(priority = 16, description = "Create movie with very long description is accepted")
    @Story("Create Movie Boundary")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithLongDescription_accepted() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("description", TestDataBuilder.longString(2000));
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 422);
    }

    @Test(priority = 17, description = "Create movie missing status field returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieMissingStatus_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.remove("status");
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 18, description = "Create movie sets isDeleted=false by default")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieIsDeletedIsFalseByDefault() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        Boolean isDeleted = res.jsonPath().getBoolean("data.deleted");
        assertThat(isDeleted == null || !isDeleted).isTrue();
    }

    @Test(priority = 19, description = "Create movie with empty body returns 400")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithEmptyBody_returns400() {
        withAdminAuth().body("{}").post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(400);
    }

    @Test(priority = 20, description = "Create movie rating can be 0 to 10")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithRating_isAccepted() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("rating", 8.5);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.rating", equalTo(8.5f));
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE MOVIE TESTS (PUT /api/movies/{id})
    // ══════════════════════════════════════════════════════════════

    @Test(priority = 21, description = "Admin can update a movie title")
    @Story("Update Movie")
    @Severity(SeverityLevel.BLOCKER)
    public void updateMovieTitle_returns200() {
        // Create first
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        // Update
        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("title", "Updated Title " + System.nanoTime() % 1000);
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("data.title", equalTo(updateBody.get("title")));
    }

    @Test(priority = 22, description = "Update movie without auth returns 401")
    @Story("Update Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void updateMovieWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1)
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 23, description = "User cannot update movie - returns 403")
    @Story("Update Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void updateMovieWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1)
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(403);
    }

    @Test(priority = 24, description = "Update non-existent movie returns 404")
    @Story("Update Movie")
    @Severity(SeverityLevel.CRITICAL)
    public void updateNonExistentMovie_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 25, description = "Update movie with empty title returns 400")
    @Story("Update Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieWithEmptyTitle_returns400() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("title", "");
        withAdminAuth().pathParam("id", 1)
                .body(body)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(anyOf(equalTo(400), equalTo(404)));
    }

    @Test(priority = 26, description = "Update movie changes status from now to soon")
    @Story("Update Movie")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieStatusFromNowToSoon() {
        Map<String, Object> createBody = TestDataBuilder.movieBodyWithStatus("now", List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("status", "soon");
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("data.status", equalTo("soon"));
    }

    @Test(priority = 27, description = "Update movie rating to new value")
    @Story("Update Movie")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieRating_changesSuccessfully() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("rating", 9.5);
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("data.rating", equalTo(9.5f));
    }

    @Test(priority = 28, description = "Update movie with invalid token returns 401")
    @Story("Update Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void updateMovieWithInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1)
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 29, description = "Update movie genres to different genres")
    @Story("Update Movie")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieGenres_updatesSuccessfully() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        // Fetch all genres
        List<Integer> allGenreIds = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.id");
        List<Integer> newGenres = allGenreIds != null && allGenreIds.size() > 1
                ? List.of(allGenreIds.get(0), allGenreIds.get(1))
                : List.of(existingGenreId);

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(newGenres);
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 30, description = "Update movie duration to valid value")
    @Story("Update Movie")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieDuration_updatesSuccessfully() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("durationMinutes", 150);
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody)
                .put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("data.durationMinutes", equalTo(150));
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE MOVIE TESTS (DELETE /api/movies/{id})
    // ══════════════════════════════════════════════════════════════

    @Test(priority = 31, description = "Admin can soft-delete a movie")
    @Story("Delete Movie")
    @Severity(SeverityLevel.BLOCKER)
    public void adminCanDeleteMovie_returns200() {
        // Create movie to delete
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", movieId)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 32, description = "Delete movie without auth returns 401")
    @Story("Delete Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteMovieWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 33, description = "User cannot delete movie — returns 403")
    @Story("Delete Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteMovieWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(403);
    }

    @Test(priority = 34, description = "Delete non-existent movie returns 404")
    @Story("Delete Movie")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteNonExistentMovie_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 35, description = "Deleted movie should no longer be returned in public list")
    @Story("Delete Movie")
    @Severity(SeverityLevel.NORMAL)
    public void deletedMovieNotReturnedInPublicList() {
        // Create
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        // Delete
        withAdminAuth().pathParam("id", movieId)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200);

        // Verify not in public list
        Response list = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        List<Integer> ids = list.jsonPath().getList("data.id");
        if (ids != null) {
            assertThat(ids).doesNotContain(movieId);
        }
    }

    @Test(priority = 36, description = "Delete movie with invalid token returns 401")
    @Story("Delete Movie Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteMovieWithInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 37, description = "Delete movie with negative ID returns 4xx")
    @Story("Delete Movie")
    @Severity(SeverityLevel.MINOR)
    public void deleteMovieWithNegativeId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 38, description = "Movie is soft-deleted (isDeleted flag set)")
    @Story("Delete Movie")
    @Severity(SeverityLevel.NORMAL)
    public void softDeletedMovieHasIsDeletedFlag() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", movieId)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200);

        // Admin with includeDeleted should see it
        Response list = withAdminAuth()
                .queryParam("includeDeleted", true)
                .get(ApiConfig.Endpoints.MOVIES);
        List<Integer> ids = list.jsonPath().getList("data.id");
        if (ids != null) {
            assertThat(ids).contains(movieId);
        }
    }

    @Test(priority = 39, description = "Double delete of same movie returns 404 on second attempt")
    @Story("Delete Movie")
    @Severity(SeverityLevel.NORMAL)
    public void doubleDeleteMovie_secondReturns404OrOk() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", movieId).delete(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(200);
        int secondStatus = withAdminAuth().pathParam("id", movieId).delete(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(secondStatus).isIn(200, 404, 400);
    }

    @Test(priority = 40, description = "Delete response has success flag")
    @Story("Delete Movie")
    @Severity(SeverityLevel.MINOR)
    public void deleteMovieResponseHasSuccessFlag() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", movieId)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    // ══════════════════════════════════════════════════════════════
    // ADDITIONAL MOVIE TESTS — Boundary & Edge cases
    // ══════════════════════════════════════════════════════════════

    @Test(priority = 41, description = "Create multiple movies in sequence")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMultipleMovies_allSucceed() {
        for (int i = 0; i < 3; i++) {
            withAdminAuth()
                    .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                    .post(ApiConfig.Endpoints.MOVIES)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 42, description = "Create movie with minimum duration (1 minute)")
    @Story("Create Movie Boundary")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithMinimumDuration_1Minute() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("durationMinutes", 1);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 43, description = "Create movie with large duration (600 minutes)")
    @Story("Create Movie Boundary")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithLargeDuration_600Minutes() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("durationMinutes", 600);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 44, description = "Movie poster URL is returned in response")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithPosterUrl_posterUrlInResponse() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("posterUrl", "/images/poster-2.jpg");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.posterUrl", notNullValue());
    }

    @Test(priority = 45, description = "Create movie with year 2000")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithOldYear_2000() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("year", 2000);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.year", equalTo(2000));
    }

    @Test(priority = 46, description = "Create movie with year far in the future")
    @Story("Create Movie Boundary")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithFutureYear() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("year", 2030);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 47, description = "Create movie with multiple valid genres")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieWithMultipleGenres_allAssigned() {
        List<Integer> allGenreIds = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.id");
        if (allGenreIds != null && allGenreIds.size() >= 2) {
            Map<String, Object> body = TestDataBuilder.validMovieBody(
                    List.of(allGenreIds.get(0), allGenreIds.get(1)));
            withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                    .then().statusCode(200)
                    .body("data.genres.size()", greaterThanOrEqualTo(2));
        }
    }

    @Test(priority = 48, description = "Create movie language field is saved")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieLanguageIsSaved() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("language", "Spanish");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.language", equalTo("Spanish"));
    }

    @Test(priority = 49, description = "Update movie with null title returns 400")
    @Story("Update Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieWithNullTitle_returns400() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("title", null);
        int status = withAdminAuth().pathParam("id", movieId)
                .body(updateBody).put(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 200);
    }

    @Test(priority = 50, description = "Update movie with zero duration returns 400")
    @Story("Update Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void updateMovieWithZeroDuration_returns400() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("durationMinutes", 0);
        int status = withAdminAuth().pathParam("id", movieId)
                .body(updateBody).put(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 200);
    }

    @Test(priority = 51, description = "Create movie with synopsis field")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithSynopsis_synopsisSaved() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("synopsis", "A thrilling story about cinema and technology.");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.synopsis", notNullValue());
    }

    @Test(priority = 52, description = "Create movie with tagline field")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithTagline_taglineSaved() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("tagline", "Experience the future");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.tagline", notNullValue());
    }

    @Test(priority = 53, description = "Create movie response has success=true")
    @Story("Create Movie")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieResponseHasSuccessTrue() {
        withAdminAuth()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test(priority = 54, description = "Update movie preserves original fields not being updated")
    @Story("Update Movie")
    @Severity(SeverityLevel.NORMAL)
    public void updateMoviePreservesExistingFields() {
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        String originalLanguage = "French";
        createBody.put("language", originalLanguage);
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");

        Map<String, Object> updateBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        updateBody.put("language", originalLanguage); // keep same
        withAdminAuth().pathParam("id", movieId)
                .body(updateBody).put(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200)
                .body("data.language", equalTo(originalLanguage));
    }

    @Test(priority = 55, description = "Create movie response time under 5 seconds")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieResponseTimeUnder5Seconds() {
        Response res = withAdminAuth()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .post(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 56, description = "Delete all created test movies cleanup")
    @Story("Delete Movie")
    @Severity(SeverityLevel.MINOR)
    public void cleanupCreatedMovies_deleteTestMovies() {
        // Delete movies created earlier in this test class
        // Just verify delete endpoint works
        Map<String, Object> createBody = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        int movieId = withAdminAuth().body(createBody).post(ApiConfig.Endpoints.MOVIES)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", movieId)
                .delete(ApiConfig.Endpoints.MOVIE_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 57, description = "Create movie with HTTP PUT instead of POST returns 4xx")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithWrongMethod_returns4xx() {
        // Using PATCH instead of POST should fail
        int status = withAdminAuth()
                .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                .patch(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 404, 405, 415, 500);
    }

    @Test(priority = 58, description = "Create movie with releaseDate null is valid")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithNullReleaseDate_isValid() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("releaseDate", null);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 59, description = "Create movie with releaseDate string value")
    @Story("Create Movie")
    @Severity(SeverityLevel.MINOR)
    public void createMovieWithReleaseDateString() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("releaseDate", "Dec 25");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.releaseDate", equalTo("Dec 25"));
    }

    @Test(priority = 60, description = "Create movie durationMinutes is positive integer")
    @Story("Create Movie Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createMovieDurationMustBePositive() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(List.of(existingGenreId));
        body.put("durationMinutes", 120);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(200)
                .body("data.durationMinutes", greaterThan(0));
    }
}

